app.controller('payController' ,function($scope ,$location,payService){
    //�������ɶ�ά��
    $scope.createNative=function(){
        payService.createNative().success(
            function(response){
                $scope.money= response.total_fee;//���
                $scope.out_trade_no= response.out_trade_no;//������
                //��ά��
                var qr = new QRious({
                    element:document.getElementById('qrious'),
                    size:250,
                    level:'H',
                    value:response.qrcode
                });
                queryPayStatus(response.out_trade_no);//��ѯ֧��״̬
            }
        );
    }
    // ��ѯ֧��״̬
    queryPayStatus = function(out_trade_no) {
        payService.queryPayStatus(out_trade_no).success(function(response) {
            if (response.success) {
                $scope.paySataus=0;
                location.href="paysuccess.html#?money="+$scope.money;
            } else {
                console.log(response.message);
                if(response.message == '��ά�볬ʱ'){
                    $scope.paySataus=1;
                }
                else if(response.message = "δ����׳�ʱ�ر�"){
                    $scope.paySataus=2;
                }else{
                    $scope.paySataus=0;
                    // location.href="payfail.html";
                }
            }
        });
    }
    //��ȡ���
    $scope.getMoney=function(){
        return $location.search()['money'];
    }

});
