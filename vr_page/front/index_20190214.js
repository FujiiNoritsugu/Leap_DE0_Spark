require('aframe');
const socket = require('socket.io-client')('http://192.168.1.8:5000');

// エンティティを保持しておくリスト
const planet_list = [];

const init_fn = ()=>{
    //console.log('connect!!');
    const sceneEl = document.querySelector('a-scene');
    const center_obj = document.querySelector('#target_box');
    for(let i=0; i < 10; i++){
        //const entityEl = document.createElement('a-entity_'+i);
        const entityEl = document.createElement('a-entity');
        entityEl.setAttribute('planet','')
        entityEl.setAttribute('follow', {init_angle:i*36,center:center_obj, speed:1});
        planet_list.push(entityEl);
        sceneEl.appendChild(entityEl);
    }
    // 3秒ごとにサーバにデータ受信を要求する
    fetch_data(1);
    //console.log('end !!');
    };

const fetch_data = (waitsec) =>{
    //console.log('request_data');
    socket.emit('request_data',{});
    setTimeout(fetch_data, waitsec*1000, waitsec);
};

socket.on('connect', init_fn);
socket.on('sensor_data', (data)=>{
    //console.log('sensor_data');
    change_move(data);
});
socket.on('disconnect', function(){});

// コンポーネントの動きを変更する
const change_move = (sensor_data) =>{
    //console.log('change_move');
    let index = 0;
    try {
        data_list = sensor_data['sensor_data'];
        for(let data of data_list){
            // エンティティの公転半径、自身の半径、色を変更する
            const target_planet = planet_list[index];
            const distance = data / 100;
            const speed = data / 100;
            target_planet.setAttribute('follow', {distance:distance, speed:speed});
            //target_planet.setAttribute('planet', {radius:data/5, color:get_color(data)});
            index++;
        }
    }catch (e) {
        console.error(e);
    }
};
// データの値からカラーコードを取得する
const get_color = (data) =>{
    const base = '3399';
    let num = parseInt(data*2);
    if (num > 255){
        num = 255;
    }

    let body = num.toString(16).toUpperCase();
    if(body.length == 1){
        body = '0' + body;
    }
    return '#' + body + base;
};

AFRAME.registerComponent('planet', {
    schema: {
      radius: {type: 'number', default: 0.5},
      width_seg: {type: 'number', default: 30},
      height_seg: {type: 'number', default: 30},
      color: {type: 'color', default: '#77EEFF'},
      event: {type: 'string', default: ''},
      message: {type: 'string', default: 'Hello, World!'}  
    },
  
    /**
     * Initial creation and setting of the mesh.
     */
    init: function () {
      var data = this.data;
      var el = this.el;
  
      // Create geometry.
      this.geometry = new THREE.SphereGeometry(data.radius, data.width_seg, data.height_seg);
  
      // Create material. 
      this.material = new THREE.MeshStandardMaterial({color: data.color});
  
      // Create mesh.
      this.mesh = new THREE.Mesh( this.geometry, this.material );
        
      // Set mesh on entity.
      el.setObject3D('planet', this.mesh);
    },

    update: function () {
      // Create geometry.
      console.log('update planet');
      // data変更時に再度オブジェクトを作り直す
      this.init();
    }
  });

AFRAME.registerComponent('follow', {
    schema:{
        init_angle:{type:'number', default:0},
        center:{type: 'selector'},
        speed:{type: 'number'},
        distance:{type: 'number', default:3}
    },

    init: function(){
        //初期値の角度を設定する
        this.angle = this.data.init_angle;
    },

    tick: function(time, timeDelta){
        // ここで円運動を行うロジック
        // 中心オブジェクトの座標
        const centerPosition = this.data.center.object3D.position;

        // 中心オブジェクトと対象オブジェクトの角度
        // XY平面上だけの角度をとりたい

        // 中心オブジェクトを原点とした座標の確認
        const z_angle = this.angle * 3;
        const new_x = centerPosition.x + Math.cos(this.angle*(Math.PI/180.0)) * this.data.distance;
        const new_y = centerPosition.y + Math.sin(this.angle*(Math.PI/180.0)) * this.data.distance;
        const new_z = centerPosition.z + Math.sin(z_angle*(Math.PI/180.0)) * this.data.distance;
        this.angle += this.data.speed;

        if (this.angle > 360.0){
            this.angle = 0.0;
        }

        // ポジションを設定する
        this.el.setAttribute('position', {
            x: new_x,
            y: new_y,
            z: new_z
        });
    }

});
