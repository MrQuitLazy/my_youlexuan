//seckill_goods控制层 
app.controller('seckillGoodsController' ,function($interval,$scope, $location,seckillGoodsService){

	//提交订单
	$scope.submitOrder=function(){
		seckillGoodsService.submitOrder($scope.entity.id).success(
			function(response){
				if(response.success){
					alert("下单成功，请在1分钟内完成支付");
					location.href="pay.html";
				}else{
					alert(response.message);
				}
			}
		);
	}

//转换秒为天小时分钟秒，格式：XXX天 10:22:33
	convertTimeString = function(allsecond){
		var days= Math.floor(allsecond/(60*60*24));//天数
		var hours= Math.floor((allsecond-days*60*60*24)/(60*60));//小时数
		var minutes= Math.floor((allsecond -days*60*60*24 - hours*60*60)/60);//分钟数
		var seconds= allsecond -days*60*60*24 - hours*60*60 -minutes*60; //秒数
		var timeString="";
		if(days>0){
			timeString=days+"天 ";
		}
		return timeString+hours+":"+minutes+":"+seconds;
	}


//读取列表数据绑定到表单中
	$scope.findList=function(){
		seckillGoodsService.findList().success(
			function(response){
				$scope.list=response;

			}
		);
	}

	//查询实体
	$scope.findOne=function(){
		seckillGoodsService.findOne($location.search()['id']).success(
			function(response){
				$scope.entity= response;

				allsecond = Math.floor((new Date($scope.entity.endTime).getTime()- new Date().getTime()) / 1000); //总秒数
				time = $interval(function(){
					if(allsecond > 0){
						allsecond = allsecond-1;
						$scope.timeString = convertTimeString(allsecond);//转换时间字符串
					}else{
						$interval.cancel(time);
						alert("秒杀服务已结束");
					}
				},1000);

			}
		);
	}

	/*******************************这是分割线，以上功能都是用到的**********************************/

	// 保存
	$scope.save = function() {
		seckillGoodsService.save($scope.entity).success(function(response) {
			if (response.success) {
				// 重新加载
				$scope.reloadList();
			} else {
				alert(response.message);
			}
		});
	}

	//批量删除 
	$scope.dele = function(){			
		//获取选中的复选框			
		seckillGoodsService.dele($scope.selectIds).success(
			function(response){
				if(response.success){
					$scope.reloadList();
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	// 定义搜索对象 
	$scope.searchEntity = {};
	// 搜索
	$scope.search = function(page,size){			
		seckillGoodsService.search(page,size,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;
			}			
		);
	}
    
});	
