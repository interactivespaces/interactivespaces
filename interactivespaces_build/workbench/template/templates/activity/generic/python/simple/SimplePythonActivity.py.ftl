from interactivespaces.activity.impl import BaseActivity

class SimplePythonActivity(BaseActivity):
    def onActivityStartup(self):
        self.log.info("Activity ${project.activity.identifyingName} startup")

    def onActivityShutdown(self):
        self.log.info("Activity ${project.activity.identifyingName} shutting down")

    def onActivityActivate(self):
        self.log.info("Activity ${project.activity.identifyingName} activated")

    def onActivityDeactivate(self):
        self.log.info("Activity ${project.activity.identifyingName} deactivated")