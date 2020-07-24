//seckill_goods服务层
app.service('seckillGoodsService', function($http){

	//提交订单
	this.submitOrder=function(seckillId){
		return $http.get('seckillOrder/submitOrder.do?seckillId='+seckillId);
	}


//读取列表数据绑定到表单中
	this.findList=function(){
		return $http.get('seckillGoods/findList.do');
	}

	this.findOne=function(id){
		return $http.get('seckillGoods/findOneFromRedis.do?id='+id);
	}


/*******************************这是分割线，以上功能都是用到的**********************************/
	// 保存、修改
	this.save = function(entity) {
		var methodName = 'add'; 	// 方法名称
		if (entity.id != null) { 	// 如果有ID
			methodName = 'update'; 	// 则执行修改方法
		}
		return $http.post('../seckillGoods/' + methodName + '.do', entity);
	}

	// 查询单个实体
	this.findOne = function(id) {
		return $http.get('../seckillGoods/findOne.do?id=' + id);
	}

	// 批量删除
	this.dele = function(ids) {
		// 获取选中的复选框
		return $http.get('../seckillGoods/delete.do?ids=' + ids);
	}

	// 查询
	this.search = function(page, size, searchEntity) {
		// post提交，page、size属性和之前相同，将searchEntity提交至后台@RequestBody对应的属性
		return $http.post('../seckillGoods/search.do?page=' + page + '&size=' + size,
				searchEntity);
	}

});