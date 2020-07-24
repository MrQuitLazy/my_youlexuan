app.controller('searchController',function($scope,$location,searchService){
    //搜索
    $scope.search=function(){
        $scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo);
        searchService.search( $scope.searchMap ).success(
            function(response){
                $scope.resultMap=response;//搜索返回的结果
                detalPage();
            }
        );
    }

    $scope.searchMap={
        'keywords':'',      //关键词
        'category':'',      //分类
        'brand':'',         //品牌
        'spec':{},          //规格
        'price':'',          //价格区间
        'pageSize':20,      //页面容量
        'pageNo':1,         //当前页
        'sort':'',
        'sortField':''

    };//搜索对象
    //添加搜索项
    $scope.addSearchItem=function(key, value){
        if(key=='category' || key=='brand' || key=='price'){//如果点击的是分类或者是品牌
            $scope.searchMap[key]=value;
        }else{
            $scope.searchMap.spec[key]=value;
        }
        $scope.search();//执行搜索
    }
    //移除复合搜索条件
    $scope.removeSearchItem=function(key){
        if(key=="category" ||  key=="brand" || key=='price'){//如果是分类或品牌
            $scope.searchMap[key]="";
        }else{//否则是规格
            delete $scope.searchMap.spec[key];//移除此属性
        }

        $scope.search();//执行搜索
    }
    $scope.savePrice = function (pric) {
        $scope.searchMap.price=pric;
        $scope.search();
    }

    $scope.changePage=function (page) {
        $scope.searchMap.pageSize="20";
        $scope.searchMap.pageNo=page;
        $scope.search();
    }


    detalPage=function(){
        $scope.perDot=true;
        $scope.postDot=true;
        $scope.pageLabel = []; // 新增分页栏属性
        var maxPageNo = $scope.resultMap.totalPages;// 得到页码总数
        var firstPage = 1;// 开始页码
        var lastPage = maxPageNo;// 截止页码
        if (maxPageNo > 5) { // 如果总页数大于5页,显示部分页码
            if ($scope.searchMap.pageNo <= 3) {
                lastPage = 5; // 前5页
                $scope.perDot=false;
            } else if ($scope.searchMap.pageNo >= maxPageNo - 2) {// 如果当前页大于等于最大页码-2
                firstPage = maxPageNo - 4; // 后5页
                $scope.postDot=false;
            } else { // 显示当前页为中心的5页
                firstPage = $scope.searchMap.pageNo - 2;
                lastPage = $scope.searchMap.pageNo + 2;
            }
        }else{
            $scope.perDot=false;
            $scope.postDot=false;
        }
        // 循环产生页码标签
        for (var i = firstPage; i <= lastPage; i++) {
            $scope.pageLabel.push(i);
        }

    }

    $scope.changePage = function (page) {
        if(page>=1 && page <= $scope.resultMap.totalPages){
            $scope.searchMap.pageNo=page;         //当前页
            $scope.search();
        }
    }
    $scope.setSort = function (sortField,sort) {
        $scope.searchMap.sortField = sortField;
        $scope.searchMap.sort = sort;
        $scope.search();
    }
    
    $scope.loadSearch = function () {
        var s = $location.search()['keywords'];
        if(s!=null && s!=""){
            $scope.searchMap.keywords=s;
            $scope.search();
        }
    }
});
