frostbite.controller('UsersCtrl', ['$scope', '$filter', '$http', '$q', function ($scope, $filter, $http, $q) {

    $scope.getAllUsers = function() {
        $http.get('/api/users/all').success(function(data) {
            // $scope.allUsers = data;
            console.log(data);
            $scope.users = data;
            _.forEach($scope.users, function(user){
                user.picture = $scope.getPicture(user.email);
            });
        }).error(function() {
            $scope.users = [];
        });
    }

    $scope.getPicture = function(email) {
        var emailLowerCase = email.toLowerCase();
        var hashEmail = CryptoJS.MD5( emailLowerCase );
        return "http://www.gravatar.com/avatar/" + hashEmail + "?s=250";
    }
}]);