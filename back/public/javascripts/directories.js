
frostbite.filter('isDirectory', function() {
    return function(input) {
        if (input.isDir) {
	    return "/assets/images/directory.png"
	} else {
	    return "/assets/images/file.png"
	}	    
    }
})

.directive('customPopover', function () {
	return {
		link: function (scope, el, attrs) {
			var jsonObj = jQuery.parseJSON(attrs.popoverHtml);
			var name = jsonObj.user.name;
			var imageString = jsonObj.user.picture;
			var files = jsonObj.files;
			var fileString = "<b>Viewing: </b><div>";
			for (var i = 0; i < files.length; i++) {
				var filepath = files[i];
				fileString += filepath.replace(/^.*[\\\/]/, '') + "\n";
			}
			fileString += "</div>";
			var image = '<img src="' + imageString + '" style="float:left;margin-right:5px;margin-bottom:10px;">';
			var htmlContent = '<p style="">' + image + '<b>' + name + '</b>' + '<div>' + fileString +'</div>' + '</p>';
			$(el).popover({
				trigger: 'hover',
				html: true,
				delay: { show: 500, hide: 100 },
				content: htmlContent,
				placement: 'bottom',
				template: '<div style="width:300px" class="popover" role="tooltip"><h3 class="popover-title"></h3><div class="popover-content"></div></div>'
			});
		}
	};
})
.controller('DirecCtrl', ['$scope', '$filter', '$http', '$timeout', function($scope, $filter, $http, $timeout) {
    $scope.filename = "";

    $scope.items = []
    var orderBy = $filter('orderBy'); 
	$scope.order = function() {
		$scope.items = orderBy($scope.items, ['-isDir', 'name'], false);
	}

    $scope.setFilename = function(filename) {
		$scope.items = filename;
		$scope.order();
		$scope.pathArray = $scope.items[0].path.split("/");
		// For windows
		if($scope.pathArray.length == 1){
			$scope.pathArray = $scope.items[0].path.split("\\");
		}
		$scope.pathArray.pop();
		for (var i = 0; i < $scope.items.length; i++) {
			//TODO: add name
			if ($scope.items[i].lastUpdated != 0) {
				$scope.items[i].editedlast = {"id":7,  "time":new Date($scope.items[i].lastUpdated)};
			} 
		}
		$scope.getUsers();
    }

	$scope.getUsers = function() {
		for (var i = 0; i < $scope.items.length; i++) {
			(function(i) {
				$http.get("/api/currently/viewing/" + $scope.items[i].path).success(function(data) {
					$scope.items[i].users = data;
					//TODO: uncomment if we want to see multiple users
					/*			
					if ($scope.items[i].users.length == 1) {
						$scope.items[0].users.push ({ "user":{"name":"Sandy Maguire", "email":"sandy.g.maguire@gmail.com"}, "files":["back/app/controllers/FileMetricsController.scala"]});

					}*/
					var numUsers = $scope.items[i].users.length;
					for (var j = 0; j < numUsers; j++) {
						var emailLowerCase = $scope.items[i].users[j].user.email.toLowerCase();
						var hashEmail = CryptoJS.MD5( emailLowerCase );  
						$scope.items[i].users[j].user.picture = "http://www.gravatar.com/avatar/" + hashEmail;
					}
				});
			}(i));
		}

		var userTimeout = $timeout($scope.getUsers,60000);
	}
 
}])

