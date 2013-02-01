import groovy.text.SimpleTemplateEngine
import groovy.util.BuilderSupport
import java.util.List
import java.util.Collections
import java.util.ArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.Future

class Master {
  String uri
  String networkType
}

class Controller {
  String type
  String folder
  String image
  String hostId
  String name
  String description
  String uuid
  List tags
  Machine machine
}

class Machine {
  String os
  String account
  String host
  String hostId
  String systemFolder
  List controllers = new ArrayList()
}

abstract class MachineOperator {
  def deploy(controller, space, options, errors) {
    def machine = controller.machine
    println "Deploying controller ${controller.name} to ${machine.host}"

    if (options.initial) {
      doCommand(machine, "mkdir -p ${controller.folder}", options, errors)
    } else {
      doCommand(machine, "cp -r ${controller.folder} ${controller.folder}_`date '+%Y%m%d_%H:%M'`", options, errors)

      doCommand(machine, "rm -fR ${controller.folder}/bootstrap/*", options, errors)
      doCommand(machine, "rm -fR ${controller.folder}/lib/system/java/*", options, errors)
      doCommand(machine, "rm -fR ${controller.folder}/bin/*", options, errors)
      doCommand(machine, "rm -fR ${controller.folder}/interactivespaces-launcher-*", options, errors)
    }

    doRecursiveCopy(machine, "${controller.image}/controller", controller.folder, options, errors)

    if (options.initial)
      sendConfigurations(controller, space, options, errors)
  }

  def setupAutostart(controller, space, options, errors) {
    println "Autostart unsupported"
  }

  def startControllers(controller, space, options, errors) {
    println "startcontrollers unsupported"
  }

  def stopControllers(controller, space, options, errors) {
    println "stopcontrollers unsupported"
  }

  def sendConfigurations(controller, space, options, errors) {
    def machine = controller.machine
    def templateEngine = new SimpleTemplateEngine()
    def binding = [controller: controller, machine: machine, space: space]

    def container = renderTemplate("${controller.image}/configs/container.conf", binding, templateEngine)
    def f1 = File.createTempFile('container-', '.conf')
    f1 << container
    def dest1 = "${controller.folder}/config/container.conf"
    doCommand(machine, "touch ${dest1}", options, errors)
    doCopy(machine, f1.absolutePath,  dest1, options, errors)

    def controllerinfo = renderTemplate("${controller.image}/configs/controllerinfo.conf", binding, templateEngine)
    def f2 = File.createTempFile('controllerinfo', '.conf')
    f2 << controllerinfo
    def dest2 = "${controller.folder}/config/interactivespaces/controllerinfo.conf"
    doCommand(machine, "touch ${dest2}", options, errors)
    doCopy(machine, f2.absolutePath, dest2, options, errors)
  }

  def renderTemplate(src, binding, templateEngine) {
    def f = new File(src)
    def template = templateEngine.createTemplate(f).make(binding)
    return template.toString()
  }

  protected void doCommand(machine, command, options, errors, background=false) {
   def extra = background ? "-f -n" : ""
   doLocalCommand("ssh ${extra} ${machine.account}@${machine.host} ${command}", options, errors)
  }  

  protected void doCopy(machine, src, dest, options, errors) {
    doLocalCommand("scp ${src} ${machine.account}@${machine.host}:${dest}", options, errors)
  }

  protected void doRecursiveCopy(machine, src, dest, options, errors) {
    def files = []
    new File(src).eachFile { files << it.absolutePath }

    doLocalCommand("scp -r ${files.join(" ")} ${machine.account}@${machine.host}:${dest}", options, errors)
  }

  protected void doLocalCommand(cmd, options, errors) {
    if (options.verbose || options.test)
      println cmd

    if (!options.test) {
      Process p = cmd.execute()
      p.waitFor()
      if (p.exitValue()) {
        errors << "$cmd\n${p.err.text} ${p.exitValue()}"
      }
    } 
  }
}

class OsxMachineOperator extends MachineOperator {
  def setupAutostart(controller, space, options, errors) {
    def machine = controller.machine
    println "Setting up autostart for ${controller.name} on ${machine.host}"

    doCommand(machine, "mkdir -p ${autostartFolder(machine)}", options, errors)

    def templateEngine = new SimpleTemplateEngine()
    def binding = [controller: controller, machine: machine, space: space]

    def keepaliveFile = keepaliveFile(machine, controller)
    binding.put('keepaliveFile', keepaliveFile)

    def plist = renderTemplate("${controller.image}/scripts/interactivespaces-controller.plist", binding, templateEngine)
    def f1 = File.createTempFile('interactivespaces-controller-', '.plist')
    f1 << plist
    def dest1 = launchdFile(machine, controller)
    doCommand(machine, "touch ${dest1}", options, errors)
    doCopy(machine, f1.absolutePath,  dest1, options, errors)

    doCommand(machine, "mkdir -p ${machine.systemFolder}", options, errors)
    doCommand(machine, "touch ${keepaliveFile}", options, errors)
  }

  def startControllers(controller, space, options, errors) {
    def machine = controller.machine
    println "Start for ${controller.name} on ${machine.host}"

    doCommand(machine, "sh -c 'cd ${controller.folder} ; nohup java -server -jar interactivespaces-launcher-1.0.0.jar --noshell &>/dev/null &'", options, errors, true)
  }

  def stopControllers(controller, space, options, errors) {
    def machine = controller.machine
    println "Stop for ${controller.name} on ${machine.host}"

      doCommand(machine, "kill -9 `cat ${controller.folder}/run/interactivespaces.pid`", options, errors)
  }

  def autostartFolder(machine) {
    return "/Users/${machine.account}/Library/LaunchAgents"
  }

  def launchdFile(machine, controller) {
    return "${autostartFolder(machine)}/interactivespaces-controller-${controller.hostId}.plist"
  }

  def keepaliveFile(machine, controller) {
    return "${machine.systemFolder}/controller-${controller.hostId}.keepalive"
  }
}

public class SpaceDescription extends BuilderSupport {
  def static make(closure) {
    SpaceDescription description = new SpaceDescription()

    closure.delegate = description

    closure()

    return description
  }

  private Master master

  private Map defaultControllers = [:]

  private Machine defaultMachine

  private Machine currentMachine

  private List machines = []

  private List controllers = []

  private boolean handlingDefaults

  private machineOperators = [
    osx: new OsxMachineOperator()
  ]

  protected void setParent(Object parent, Object child) {
  }

  protected Object createNode(Object name) {
    switch (name) {
    case "defaults":
      handlingDefaults = true

      return "defaults"
    }
  }

  protected void nodeCompleted(Object parent, Object node) {
    if (node == "defaults") {
      handlingDefaults = false;
    } else if (currentMachine == node) {
      machines << node
      currentMachine = null
    }
  }

  protected Object createNode(Object name, Map attributes) {
    if (handlingDefaults) {
      switch (name) {
      case "machine":
        defaultMachine = attributes

        break

      case "controller":
        def controller = new Controller(attributes)
        defaultControllers[controller.type] = controller

        break
      }
    } else {
     // Not defaults
     switch (name) {
     case "master":
       master = new Master(attributes)
       break

     case "machine":
       if (currentMachine == null) {
         def machine = new Machine(attributes)
         fixMachineDefaults(machine)

         currentMachine = machine
       } else {
         println "WARNING: Embedded machine definition. Ignored."
       }
       break

     case "controller":
       if (currentMachine != null) {
         def controller = new Controller(attributes)
         fixControllerDefaults(controller)

         currentMachine.controllers << controller
         controller.machine = currentMachine

         controllers << controller
       } else {
         println "WARNING: Controller definition outside of machine. Ignored."
       }
       break
      }
    }
  }

  protected Object createNode(Object name, Object value) {
    println name + " with value"
  }

  protected Object createNode(Object name, Map attributes, Object value) {
    println name + " with value and attributes"
  }

  void fixControllerDefaults(Controller controller) {
    def controllerTemplate = defaultControllers["default"]
    if (controller.type != null) {
      controllerTemplate = defaultControllers[controller.type]
    }

    if (controller.image == null) {
      controller.image = controllerTemplate.image
    }

    if (controller.folder == null) {
      controller.folder = controllerTemplate.folder
    }
  }

  void fixMachineDefaults(Machine machine) {
    if (machine.os == null) {
      machine.os = defaultMachine.os
    }
    if (machine.account == null) {
      machine.account = defaultMachine.account
    }
    if (machine.systemFolder == null) {
      machine.systemFolder = defaultMachine.systemFolder
    }
  }

  def doOperation(operation, options, controllerFilters=null) {
    List errors = Collections.synchronizedList(new ArrayList());

    def c = controllers
    if (controllerFilters) {
      c = controllers.findAll {cof ->
        controllerFilters.any { f -> f(cof)}
      }
    }
    
    if (operation != 'list') {
      doConcurrently(c, { controller ->
        def operator = machineOperators[controller.machine.os]
        if (operator)
          operator."$operation"(controller, this, options, errors)
      })
    } else {
      c.each {controller -> println controller.name}
    }

    errors.each { println it }
  }

  def doConcurrently(controllers, myClosure) {
    def threadPool = Executors.newFixedThreadPool(16)
    try {
      List<Future> futures = controllers.collect{controller->
        threadPool.submit({->
          myClosure controller } as Callable);
      }
      futures.each{it.get()}
    } finally {
      threadPool.shutdown()
    }
  }
}

static ExpandoMetaClass createEMC(Class clazz, Closure cl) {
  ExpandoMetaClass emc = new ExpandoMetaClass(clazz, false)

  cl(emc)

  emc.initialize()

  return emc
}

def cli = new CliBuilder(
    usage:'SpaceOperationsRunner [options] description operation targets\noperation is one of list, deploy, startControllers, stopControllers',
    )
cli.t('Test the operation, just shows the commands what would be run')
cli.v('Be verbose, showing commands as they are being done')
cli.i('This is an initial installation')
cli.s('Space Operations folder', args: 1)

if (args) {
  def opts = cli.parse(args)
  def dfile = opts.arguments()[0]
  def operation = opts.arguments()[1]
  def description = new File(dfile).getText()

  def script = """
SpaceDescription.make {
$description
}
"""

  def desc = Eval.me(script)

  def options = [:]
  options['verbose'] = opts.v
  options['test'] = opts.t
  options['initial'] = opts.i
  println "Performing operation $operation on description $dfile"

  def error = false

  def controllerFilters = []
  if (opts.arguments().size > 2) {
    def controllers = opts.arguments()[2..-1]
    if (!controllers.isEmpty()) {
      controllers.each {e -> 
        if (e.startsWith(':')) {
          def value = e.substring(1).trim()
          if (!value) {
            println "Bad tag field $e"
            error = true
          }

          controllerFilters << { c -> (c.tags) ? c.tags.contains(value) : false }
       } else {
          controllerFilters << { c -> c.name == e}
        }
      }
    }
  }

  if (!error) {
    desc.doOperation(operation, options, controllerFilters)
  }
} else {
  println cli.usage()
}
