interactivespaces.activity.impl.BaseActivity {
    onActivitySetup: function() {
        this.getLog().info("${project.activity.identifyingName} setup");
    },

    onActivityStartup: function() {
        this.getLog().info("${project.activity.identifyingName} startup");
    },

    onActivityActivate: function() {
        this.getLog().info("Activity ${project.activity.identifyingName} activate");
    },

    onActivityDeactivate: function() {
        this.getLog().info("Activity ${project.activity.identifyingName} deactivate");
    }

    onActivityShutdown: function() {
        this.getLog().info("Activity ${project.activity.identifyingName} shutdown");
    },

    onActivityCleanup: function() {
        this.getLog().info("Activity ${project.activity.identifyingName} cleanup");
    }
}
