(function() {
  var module = angular.module('interactiveSpacesWebAdminControllers', []);

  module.controller('ActivityMainCtrl', [
      '$scope',
      '$http',
      function($scope, $http) {
        $http.get('/interactivespaces/activity/all.json').success(
            function(data) {
              $scope.activities = data.data;
            });
        
      } ]);

  module.controller('ActivityDetailCtrl', [
      '$scope',
      '$http',
      '$routeParams',
      function($scope, $http, $routeParams) {
        $http.get(
            '/interactivespaces/activity/' + $routeParams.activityId
                + '/view.json').success(function(data) {
          $scope.activityInfo = data.data;
          $scope.hasLiveActivities = $scope.activityInfo.liveactivities.length != 0;
        });
        
        this.hasLiveActivities = function() {
          return $scope.activityInfo.liveactivities.length != 0;
        }
      } ]);

  module.controller('LiveActivityMainCtrl', [
      '$scope',
      '$http',
      function($scope, $http) {
        $http.get('/interactivespaces/liveactivity/all.json').success(
            function(data) {
              $scope.liveActivities = data.data;
            });
      } ]);

  module.controller('LiveActivityDetailCtrl', [
      '$scope',
      '$http',
      '$routeParams',
      function($scope, $http, $routeParams) {
        $http.get(
            '/interactivespaces/liveactivity/' + $routeParams.liveActivityId
                + '/view.json').success(function(data) {
          $scope.liveActivityInfo = data.data;
        });
      } ]);

  module.controller('LiveActivityGroupMainCtrl', [
      '$scope',
      '$http',
      function($scope, $http) {
        $http.get('/interactivespaces/liveactivitygroup/all.json').success(
            function(data) {
              $scope.liveActivityGroups = data.data;
            });
      } ]);

  module.controller('LiveActivityGroupDetailCtrl', [
      '$scope',
      '$http',
      '$routeParams',
      function($scope, $http, $routeParams) {
        $http.get(
            '/interactivespaces/liveactivitygroup/'
                + $routeParams.liveActivityGroupId + '/view.json').success(
            function(data) {
              $scope.liveActivityGroupInfo = data.data;
            });
      } ]);

  module.controller('SpaceMainCtrl', [ '$scope', '$http',
      function($scope, $http) {
        $http.get('/interactivespaces/space/all.json').success(function(data) {
          console.log(data.data);
          $scope.spaces = data.data;
        });
      } ]);

  module.controller('SpaceDetailCtrl', [
      '$scope',
      '$http',
      '$routeParams',
      function($scope, $http, $routeParams) {
        $http.get(
            '/interactivespaces/space/' + $routeParams.spaceId + '/view.json')
            .success(function(data) {
              $scope.spaceInfo = data.data;
            });
      } ]);

  module.controller('SpaceControllerMainCtrl', [
      '$scope',
      '$http',
      function($scope, $http) {
        $http.get('/interactivespaces/spacecontroller/all.json').success(
            function(data) {
              $scope.spaceControllers = data.data;
            });
      } ]);

  module.controller('SpaceControllerDetailCtrl', [
      '$scope',
      '$http',
      '$routeParams',
      function($scope, $http, $routeParams) {
        $http.get(
            '/interactivespaces/spacecontroller/'
                + $routeParams.spaceControllerId + '/view.json').success(
            function(data) {
              $scope.spaceControllerInfo = data.data;
            });
      } ]);

})();