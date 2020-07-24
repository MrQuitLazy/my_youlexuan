//item_cat控制层 
app.controller('itemCatController' ,function($scope, $controller, itemCatService,typeTemplateService){
	
	// 继承
	$controller("baseController", {
		$scope : $scope
	});
	
	// 保存
	$scope.save = function() {
		itemCatService.save($scope.entity).success(function(response) {
			if (response.success) {
				// 重新加载
				$scope.findByParentId($scope.parentEntity);
			} else {
				alert(response.message);
			}
		});
	}
	$scope.add = function() {
			$scope.entity.parentId=$scope.parentEntity.id;
		itemCatService.save($scope.entity).success(function(response) {
			if (response.success) {
				// 重新加载
				$scope.findByParentId($scope.parentEntity);
			} else {
				alert(response.message);
			}
		});
	}
	//查询实体 
	$scope.findOne = function(id){				
		itemCatService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//批量删除 
	$scope.dele = function(){			
		//获取选中的复选框			
		itemCatService.dele($scope.selectIds).success(
			function(response){
				if(response.success){
					$("#selall").prop("checked",false);
					$scope.selectIds=[];
					$scope.findByParentId($scope.parentEntity);
				}
				alert(response.message)
			}		
		);				
	}
	
	// 定义搜索对象 
	$scope.searchEntity = {};
	// 搜索
	$scope.search = function(page,size){			
		itemCatService.search(page,size,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;
			}			
		);
	}

	$scope.parentEntity = {id:0};	//当前页面父id
	$scope.gread=0;			//当前页面级别
	$scope.entity1=null;	//被点击的一级分类
	$scope.entity2=null;	//被点击的二级分类
	//根据上级ID显示下级列表
	$scope.findByParentId=function(entity){
		if($scope.gread==0){
			$scope.entity1=null;	//被点击的一级分类
			$scope.entity2=null;	//被点击的二级分类
			$scope.parentEntity.id = 0;	//当前页面父id
		}
		if($scope.gread==1){
			$scope.entity1=entity;
			$scope.entity2=null;
			$scope.parentEntity = entity;	//当前页面父id
		}
		if($scope.gread==2){
			$scope.entity2=entity;
			$scope.parentEntity = entity;	//当前页面父id
		}
		itemCatService.findByParentId($scope.parentEntity.id).success(
			function(response){
				$scope.list=response;
			}
		);
	}
	$scope.setGread=function (i) {
		$scope.gread=i;
	}

	$scope.typeTemplateList={data:[]};//模板列表
	//读取模板列表
	$scope.findtypeTemplateList=function(){
		typeTemplateService.selectOptionList().success(
			function(response){
				$scope.typeTemplateList={data:response};
			}
		);
	}
});	
