(function() {
  var interactiveSpacesWebAdminApp = angular.module(
      'interactiveSpacesWebAdminApp', ['ngRoute',
        'interactiveSpacesWebAdminControllers', 'LiveActivityDirective']);

  interactiveSpacesWebAdminApp
      .config([
        '$routeProvider',
        function($routeProvider) {
          $routeProvider
              .when('/activity', {
                templateUrl: '/interactivespaces/static/partials/activity-main.html',
                controller: 'ActivityMainCtrl'
              })
              .when('/activity/:activityId', {
                templateUrl: '/interactivespaces/static/partials/activity-detail.html',
                controller: 'ActivityDetailCtrl'
              })
              .when('/liveactivity', {
                templateUrl: '/interactivespaces/static/partials/live-activity-main.html',
                controller: 'LiveActivityMainCtrl'
              })
              .when('/liveactivity/:liveActivityId', {
                templateUrl: '/interactivespaces/static/partials/live-activity-detail.html',
                controller: 'LiveActivityDetailCtrl'
              })
              .when('/liveactivitygroup', {
                templateUrl: '/interactivespaces/static/partials/live-activity-group-main.html',
                controller: 'LiveActivityGroupMainCtrl'
              })
              .when('/liveactivitygroup/:liveActivityGroupId', {
                templateUrl: '/interactivespaces/static/partials/live-activity-group-detail.html',
                controller: 'LiveActivityGroupDetailCtrl'
              })
              .when('/space', {
                templateUrl: '/interactivespaces/static/partials/space-main.html',
                controller: 'SpaceMainCtrl'
              })
              .when('/space/:spaceId', {
                templateUrl: '/interactivespaces/static/partials/space-detail.html',
                controller: 'SpaceDetailCtrl'
              })
              .when('/spacecontroller', {
                templateUrl: '/interactivespaces/static/partials/space-controller-main.html',
                controller: 'SpaceControllerMainCtrl'
              })
              .when('/spacecontroller/:spaceControllerId', {
                templateUrl: '/interactivespaces/static/partials/space-controller-detail.html',
                controller: 'SpaceControllerDetailCtrl'
              })
              .otherwise({
                redirectTo: '/liveactivity'
              });
        }]);
})();
