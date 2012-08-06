from interactivespaces.activity.impl import BaseActivity

class SimplePythonActivity(BaseActivity):
    def onActivitySetup(self):
        self.log.info("Activity ${project.activity.identifyingName} setup")

    def onActivityStartup(self):
        self.log.info("Activity ${project.activity.identifyingName} startup")

    def onActivityActivate(self):
        self.log.info("Activity ${project.activity.identifyingName} activated")

    def onActivityDeactivate(self):
        self.log.info("Activity ${project.activity.identifyingName} deactivated")

    def onActivityShutdown(self):
        self.log.info("Activity ${project.activity.identifyingName} shutting down")

    def onActivityCleanup(self):
        self.log.info("Activity ${project.activity.identifyingName} cleanup")
