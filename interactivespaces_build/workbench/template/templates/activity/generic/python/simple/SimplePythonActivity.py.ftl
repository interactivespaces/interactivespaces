from interactivespaces.activity.impl import BaseActivity

class SimplePythonActivity(BaseActivity):
    def onActivitySetup(self):
        self.log.info("Activity ${project.identifyingName} setup")

    def onActivityStartup(self):
        self.log.info("Activity ${project.identifyingName} startup")

    def onActivityPostStartup(self):
        self.log.info("Activity ${project.identifyingName} post startup")

    def onActivityActivate(self):
        self.log.info("Activity ${project.identifyingName} activated")

    def onActivityDeactivate(self):
        self.log.info("Activity ${project.identifyingName} deactivated")

    def onActivityPreShutdown(self):
        self.log.info("Activity ${project.identifyingName} pre shutting down")

    def onActivityShutdown(self):
        self.log.info("Activity ${project.identifyingName} shutting down")

    def onActivityCleanup(self):
        self.log.info("Activity ${project.identifyingName} cleanup")
