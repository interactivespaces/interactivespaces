def doTest(group) {
  println group.name

  spaceEnvironment.setValue('devtestrun', true)

  while (spaceEnvironment.getValue('devtestrun')) {

    activeControllerManager.deployLiveActivityGroup(group)

    Object.sleep(10000)

    activeControllerManager.startupLiveActivityGroup(group)

    Object.sleep(10000)

    activeControllerManager.activateLiveActivityGroup(group)

    Object.sleep(10000)

    activeControllerManager.shutdownLiveActivityGroup(group)

    Object.sleep(10000)
  }

}

def group
for (g in activityRepository.allLiveActivityGroups) {
  if (g.name == 'Routables') { doTest(g); break; }
}
