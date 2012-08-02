interactivespaces.activity.impl.BaseActivity {
    onActivityStartup: function() {
        this.getLog().info("${project.activity.identifyingName} startup");
    },

    onActivityShutdown: function() {
        this.getLog().info("Activity ${project.activity.identifyingName} shutdown");
    },

    onActivityActivate: function() {
        this.getLog().info("Activity ${project.activity.identifyingName} activate");
    },

    onActivityDeactivate: function() {
        this.getLog().info("Activity ${project.activity.identifyingName} deactivate");
    }
}
