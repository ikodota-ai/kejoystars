const config = {
    terminal: 1, //终端
    title: '后台管理系统', //网站默认标题
    version: '1.9.1', //版本号
    //baseUrl: `${import.meta.env.VITE_APP_BASE_URL || 'http://apiadmin.kejoystars.com:8881/'}/`, //请求接口域名
    baseUrl: `${import.meta.env.VITE_APP_BASE_URL || 'http://localhost:8082/'}/`, //请求接口域名
    urlPrefix: 'adminapi', //请求默认前缀
    timeout: 100 * 1000 //请求超时时长
}

export default config
