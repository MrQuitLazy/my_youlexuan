//购物车控制层
app.controller('cartController', function ($scope, cartService) {
    $scope.order= {paymentType: '1'};

    //选择支付方式
    $scope.selectPayType = function (type) {
        $scope.order.paymentType = type;
    }

    //选择收件地址
    $scope.selectAddres = function (addres) {
        $scope.address = addres;
    }
    //获取收件地址
    $scope.findAddressList = function () {
        cartService.findAddressList().success(
            function (resp) {
                $scope.addressList = resp;
                for (var i = 0; i < $scope.addressList.length; i++) {
                    if (resp[i].isDefault == 1) {
                        $scope.address = resp[i];
                        break;
                    }
                }

            }
        )
    }
    //查询购物车列表
    $scope.findCartList = function () {
        cartService.findCartList().success(
            function (response) {
                $scope.cartList = response;
                $scope.totalValue = cartService.sum($scope.cartList);//求合计数
            }
        );
    }

//添加商品到购物车
    $scope.addGoodsToCartList = function (itemId, num) {
        cartService.addGoodsToCartList(itemId, num).success(
            function (response) {
                if (response.success) {
                    $scope.findCartList();//刷新列表
                } else {
                    alert(response.message);//弹出错误提示
                }
            }
        );
    }
    $scope.add = function () {
        $scope.order.receiverAreaName=$scope.address.address;
        $scope.order.receiver=$scope.address.contact;
        $scope.order.receiverMobile=$scope.address.mobile;
        cartService.add($scope.order).success(
            function (response) {
                if (response.success){
                    location.href="pay.html"
                }
            }
        )
    }

});
