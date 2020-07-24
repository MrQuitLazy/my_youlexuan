//商品详细页（控制层）
app.controller('itemController',function($scope,$http){


	//添加商品到购物车
	$scope.addGoodsToCartList = function () {

		var itemId="";
		var num = "";
		$http.get('/cart/addGoodsToCartList.do?itemId='+itemId+'&num='+num).success(
			function (response) {
				if (response.success) {
					$scope.findCartList();//刷新列表
				} else {
					alert(response.message);//弹出错误提示
				}
			}
		);
	}

	$scope.num = 1;
	//数量操作
	$scope.addNum=function(x){
		$scope.num=$scope.num+x;
		if($scope.num<1){
			$scope.num=1;
		}
	}
	
	$scope.specificationItems={};//记录用户选择的规格
	//用户选择规格
	$scope.selectSpecification=function(name,value){
		$scope.specificationItems[name]=value;
		searchSku();//读取sku

	}	
	//判断某规格选项是否被用户选中
	$scope.isSelected=function(name,value){
		if($scope.specificationItems[name]==value){
			return true;
		}else{
			return false;
		}
	}

	//加载默认SKU
	$scope.loadSku=function(){
		$scope.sku=skuList[0];
		//默认用户选中
		$scope.specificationItems= JSON.parse(JSON.stringify($scope.sku.spec)) ;
	}
//匹配两个对象
	matchObject=function(map1,map2){
		for(var k in map1){
			if(map1[k]!=map2[k]){
				return false;
			}
		}
		for(var k in map2){
			if(map2[k]!=map1[k]){
				return false;
			}
		}
		return true;
	}
//查询SKU
	searchSku=function(){
		for(var i=0;i<skuList.length;i++ ){
			if( matchObject(skuList[i].spec ,$scope.specificationItems ) ){
				$scope.sku=skuList[i];
				return;
			}
		}
//如果某些规格选项组合起来没有商品，给出一些提示字样
		$scope.sku={id:0,title:'没有商品',price:0};
	}
	

//添加商品到购物车
	$scope.addToCart=function(){
		alert('skuid:'+$scope.sku.id);
		// youlexuan-cart-web添加购物车
		$http.get('http://localhost:9013/cart/addGoodsToCartList.do?itemId='
			+ $scope.sku.id +'&num='+$scope.num, {'withCredentials':true}).success(
			function(response){
				if(response.success){
					//跳转到购物车页面
					alert("添加购物车成功，请继续选购！")
					location.href='http://localhost:9013/cart.html';
				}else{
					alert(response.message);
				}
			}
		);
	}


});
