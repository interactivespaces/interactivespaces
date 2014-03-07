interactivespaces.activity.impl.BaseActivity {
    onActivitySetup: function() {
        this.getLog().info("${project.identifyingName} setup");
    },

    onActivityStartup: function() {
        this.getLog().info("${project.identifyingName} startup");
    },

    onActivityPostStartup: function() {
        this.getLog().info("${project.identifyingName} post startup");
    },

    onActivityActivate: function() {
        this.getLog().info("Activity ${project.identifyingName} activate");
    },

    onActivityDeactivate: function() {
        this.getLog().info("Activity ${project.identifyingName} deactivate");
    },

    onActivityPreShutdown: function() {
        this.getLog().info("Activity ${project.identifyingName} pre shutdown");
    },

    onActivityShutdown: function() {
        this.getLog().info("Activity ${project.identifyingName} shutdown");
    },

    onActivityCleanup: function() {
        this.getLog().info("Activity ${project.identifyingName} cleanup");
    }
}
