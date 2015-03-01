frostbite.controller('LoginCtrl', ['$scope','$http', '$q', function ($scope, $http, $q) {
   
$scope.init = function () {

}

$scope.loginClicked = function () {
	var username = $("#username").val();
	var password = $("#password").val();
	
	var data = {"username": username,
				"password": password};
	$http.post('/login', data).success(function(data) {
			// TODO Success handler
		}).error(function() {
			// TODO Error handler	
		});
}

$scope.init();

}]);