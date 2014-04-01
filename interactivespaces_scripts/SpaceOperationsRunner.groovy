/*
 * Copyright (C) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

def cli = new CliBuilder(
    usage:'SpaceOperationsRunner [options] description operation targets\noperation is one of list, deploy, startControllers, stopControllers, harshStopControllers, printUuids',
    )
cli.t('Test the operation, just shows the commands what would be run')
cli.v('Be verbose, showing commands as they are being done')
cli.i('This is an initial installation')
cli.u('Supply UUIDs to controllers based on the Interactive Spaces HostID')

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
  options['uuid'] = opts.u

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
