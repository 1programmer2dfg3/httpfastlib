# httpfastlib
将我这个lib作为你项目的moudle导入到你的工程中去
调用实例：
    /**
     * 请求实例
     */
    private void requestURL(String value) {
        final Request request = Request.obtain("这里面传入你的接口地址");
        request.put("key", value);//传入你需要传给后台的请求参数key和value
        request.setListener(new SimpleListener<Object>>() {//object为你返回的json数据所建立的实体类，放入里面会自动帮你解析数据

            @Override
            public void onTranslateJson(Request r, String json) {
                super.onTranslateJson(r, json);
            }

            @Override
            public void onResponseListener(Request r, Response<object> result) {//请求成功的方法
              
                    //result请求接口返回的json，然后你可以在这个方法里面取到你所需要的数据，进行你的业务逻辑

            }

            @Override
            public void onErrorListener(Request r, String error) {//请求失败的方法
                super.onErrorListener(r, error);

            }
        });
        net(request);//net是启动post请求
    }
