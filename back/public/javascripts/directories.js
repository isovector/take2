var ROOTURL = "http://0.0.0.0:9000/"

var directories = angular.module('directories', ['ui.listview'])
.filter('isDirectory', function() {
    return function(input) {
        if (input.isDirec) {
	    return ROOTURL + "assets/images/directory.png"
	} else {
	    return ROOTURL + "assets/images/file.png"
	}	    
    }
})

.controller('DirecCtrl', ['$scope', function($scope) {
  $scope.items = [
    {
      filename: "back",
      filesize: "45678",
      editedlast: new Date(),
      isDirec: true,
      subdirecs: "back2",
      currentpath:"directory/"
    },
    {
      filename: "README",
      filesize: "456",
      editedlast: new Date(),
      isDirec: false,
      subdirecs: null,
      currentpath:"directory/"
    }]
}]);
