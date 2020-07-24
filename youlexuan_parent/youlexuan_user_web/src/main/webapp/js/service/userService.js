//user服务层
app.service('userService', function($http){
	// 保存、修改
	this.save = function(entity,code) {
		return $http.post('/user/add.do?code='+code, entity);
	}
	// 保存、修改
	this.sendMsg = function(phone) {
		return $http.post('/user/sendMsg.do?phone='+phone);
	}
});