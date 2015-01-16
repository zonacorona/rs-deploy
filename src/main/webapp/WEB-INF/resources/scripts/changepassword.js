(function(){
	var changepasswordApp=angular.module("changepasswordapp",[]);
	changepasswordApp.controller("ChangepasswordController",['$http',function($http){
		this.changepasswordwidget=false;
		
		this.changeButtonColor=function(btnId,color){
			if(!$scope.disableButtons){
			    $(('#'+btnId)).css('background-color',color);
			}
		};
		
		this.chagePassword=function(){
			alert('clicked on submit');
		};
		
	}]);
})();