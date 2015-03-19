frostbite.controller('SymbolCtrl', ['$scope', '$http', '$timeout', function($scope, $http, $timeout) {
	$scope.relatedSymbols = [],

    $scope.init = function(symbolId) {
		$http.get('/api/symbols/' + symbolId).success(function (data) {
			// These are sorted by weight so we just need to display them in order
			$scope.relatedSymbols = data;
		});
	};   
}]);
