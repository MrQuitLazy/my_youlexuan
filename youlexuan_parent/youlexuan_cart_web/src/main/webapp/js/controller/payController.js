app.controller('payController' ,function($scope ,$location,payService){
    //本地生成二维码
    $scope.createNative=function(){
        payService.createNative().success(
            function(response){
                $scope.money= response.total_fee;//金额
                $scope.out_trade_no= response.out_trade_no;//订单号
                //二维码
                var qr = new QRious({
                    element:document.getElementById('qrious'),
                    size:250,
                    level:'H',
                    value:response.qrcode
                });
                queryPayStatus(response.out_trade_no);//查询支付状态
            }
        );
    }
    // 查询支付状态
    queryPayStatus = function(out_trade_no) {
        payService.queryPayStatus(out_trade_no).success(function(response) {
            if (response.success) {
                $scope.paySataus=0;
                location.href="paysuccess.html#?money="+$scope.money;
            } else {
                console.log(response.message);
                if(response.message == '二维码超时'){
                    $scope.paySataus=1;
                }
                else if(response.message = "未付款交易超时关闭"){
                    $scope.paySataus=2;
                }else{
                    $scope.paySataus=0;
                    // location.href="payfail.html";
                }
            }
        });
    }
    //获取金额
    $scope.getMoney=function(){
        return $location.search()['money'];
    }

});
