//user控制层 
app.controller('userController' ,function($scope, userService){

	
	// 保存
	$scope.save = function() {
		userService.save($scope.entity,$scope.code).success(function(response) {
			if (response.success) {
				alert("注册成功")
			} else {
				alert(response.message);
			}
		});
	}
	$scope.sendMsg = function () {
		userService.sendMsg($scope.entity.phone).success(function(response) {
			if (response.success) {
				$("#btnt").prop("disabled","disabled");
				var time = 180;
				$("#btnt").val(time+"秒后重新发送")
				var timer = setInterval(function () {
					time--;
					if(time<1){
						clearInterval(timer);

						$("#btnt").prop("disabled","");
						$("#btnt").val("获取验证码")
						return;
					}
					$("#btnt").val(time+"秒后重新发送")
				},1000)
			} else {
				alert(response.message);
			}
		});
	}
});	
