frostbite.controller('SymbolCtrl', ['$scope', '$http', '$timeout', function($scope, $http, $timeout) {
	$scope.relatedSymbols = [],

    $scope.init = function(symbolId) {
		$http.get('/api/symbols/' + symbolId).success(function (data) {
			if (!data) {
				data = [
					{
						"symbol":
						{
							"file": "/back/app/models/SymbolModel.scala",
							"line" : 20,
							"id" : 10
						},
						"weight" : .5
					},
					{
						"symbol":
						{
							"file": "/back/app/models/SymbolModel.scala",
							"line" : 30,
							"id" : 11
						},
						"weight" : .4
					}
				];
			}			
			// These are sorted by weight so we just need to display them in order
			$scope.relatedSymbols = data;
		});
	};   
}]);
