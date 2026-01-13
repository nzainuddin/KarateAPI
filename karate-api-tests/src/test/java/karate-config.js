function fn() {
  karate.log('*** CONFIG LOADED ***');
  var config = {
    username: 'demo',
    password: 'password123',
    baseUrlBPay: 'https://billpay-api.gauravkhurana-practice-api.workers.dev/v1'
  }
  karate.env = karate.env || 'dev';
  karate.configure('connectTimeout', 5000);
  karate.configure('readTimeout', 5000);
  karate.log('Config loaded with username: ' + config.username);
  karate.log('Config loaded with baseUrlBPay: ' + config.baseUrlBPay);
  return config;
}