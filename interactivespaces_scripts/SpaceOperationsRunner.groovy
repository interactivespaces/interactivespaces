def cli = new CliBuilder(
    usage:'SpaceOperationsRunner [options] description operation targets\noperation is one of list, deploy, startControllers, stopControllers',
    )
cli.t('Test the operation, just shows the commands what would be run')
cli.v('Be verbose, showing commands as they are being done')
cli.i('This is an initial installation')

if (args) {
  def opts = cli.parse(args)
  def dfile = opts.arguments()[0]
  def operation = opts.arguments()[1]
  def operations = new File('SpaceOperations.groovy').getText()
  def description = new File(dfile).getText()

  def script = """
$operations
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
