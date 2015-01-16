(function(){
	
	var loginApp=angular.module("loginapp",[]);
	
	loginApp.controller("LoginController",function(){
		this.myusername='';
		this.mypassword='';
		this.showProgress=false;
		this.usernametoolong=false;
		this.usernameerror=false;
		this.passwordtoolong=false;
		this.passworderror=false;
		this.passwordprogress=false;
		
		$('#id_password').removeAttr('style');
		$('#id_username').removeAttr('style');
		
		this.userLogin=function(){
			if(this.validateForm()){
				this.passwordprogress=true;
				$("#login-form").submit();	
			}
		};
		
		this.validateForm=function(){
			var passwordNameError=$('#id_password');
			var usernameError=$('#id_username');
			//remove any red borders to begin validation
			passwordNameError.removeAttr('style');
			usernameError.removeAttr('style');
			
			//Hide any error message associated with the user name and password
			this.usernametoolong=false;
			this.usernameerror=false;
			this.passwordtoolong=false;
			this.passworderror=false;
			//The showing of this element is done via spring MVC, it checks the session attribute loginstatus
			$('#login-status').hide();
			
			var retVal=true;

			if(this.myusername.length>20){
				retVal=false;
				usernameError.attr('style','border-color:#c40022;border-width:1px;');	
				this.usernametoolong=true;
				this.usernameerror=false;
			}
			//Username passed validation
			else{
				//Password has a value, make sure that the length is not over 100 characters
				if(this.mypassword>20){
					retVal=false;
					passwordNameError.attr('style','border-color:#c40022;border-width:1px;');
					this.passwordtoolong=true;
					this.passworderror=false;
				}					
			}			

			return retVal;
		};
	});
	
})();