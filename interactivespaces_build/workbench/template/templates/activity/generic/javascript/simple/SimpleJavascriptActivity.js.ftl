interactivespaces.activity.impl.BaseActivity {
    onActivitySetup: function() {
        this.getLog().info("${project.activityDescription.identifyingName} setup");
    },

    onActivityStartup: function() {
        this.getLog().info("${project.activityDescription.identifyingName} startup");
    },

    onActivityPostStartup: function() {
        this.getLog().info("${project.activityDescription.identifyingName} post startup");
    },

    onActivityActivate: function() {
        this.getLog().info("Activity ${project.activityDescription.identifyingName} activate");
    },

    onActivityDeactivate: function() {
        this.getLog().info("Activity ${project.activityDescription.identifyingName} deactivate");
    }

    onActivityPreShutdown: function() {
        this.getLog().info("Activity ${project.activityDescription.identifyingName} pre shutdown");
    },

    onActivityShutdown: function() {
        this.getLog().info("Activity ${project.activityDescription.identifyingName} shutdown");
    },

    onActivityCleanup: function() {
        this.getLog().info("Activity ${project.activityDescription.identifyingName} cleanup");
    }
}
