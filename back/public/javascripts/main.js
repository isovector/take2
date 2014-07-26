var frostbite = angular.module('frostbite', []);

frostbite.directive('frostbiteHeader', function() {
	return {
		//Only apply this directive to elements with this name
		restrict: 'A',
		//replace the element with the template
		replace: true,
		templateUrl: "/assets/directives/header.partial.html"
	}
});