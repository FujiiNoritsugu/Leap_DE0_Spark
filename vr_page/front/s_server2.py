import socketio
import engineio
import eventlet
import sched, time
from sensor import get_sensor
import numpy as np
import json
from concurrent import futures

sio = socketio.Server()
app = socketio.WSGIApp(sio, static_files={
    '/': {'content_type': 'text/html', 'filename': 'index.html'},
    '/favicon.ico': {'content_type': 'text/html', 'filename': 'favicon.ico'},
    '/out.js': {'content_type': 'text/javascript', 'filename': 'out.js'}
})
executor = futures.ThreadPoolExecutor(max_workers=1)

def send_data():
    '''
    データを3秒ごとに定期送信する
    '''
    schedule = sched.scheduler(time.time, time.sleep)
    while True:
        # 3秒ごとにセンサデータを送信する
        schedule.enter(3, 1, send_sensor_data)
        schedule.run()

def send_sensor_data():
    '''
    センサデータを送信する
    '''
    try:
        # 1000個のデータを10個のデータに変更し、JSONに変換する
        #sensor_data = json.dumps([np.mean(data) for data in np.split(np.array(get_sensor()), 10)])
        sensor_data = [np.mean(data) for data in np.split(np.array(get_sensor()), 10)]
        # JSONデータをクライアントに送信する
        sio.emit('sensor_data', {'sensor_data':sensor_data})
        print('send_sensor_data')
    except:
        import traceback
        traceback.print_exc()


@sio.on('connect')
def connect(sid, environ):
    # 別スレッドで定期実行処理を行う
    # executor.submit(send_data)
    print('connect ', sid)

@sio.on('request_data')
def message(sid, data):
    send_sensor_data()
    print('message ', data)

@sio.on('disconnect')
def disconnect(sid):
    # スレッドを停止する
    # executor.shutdown()
    print('disconnect ', sid)

if __name__ == '__main__':
    eventlet.wsgi.server(eventlet.listen(('', 5000)), app)
    # 非同期にデータを送信する

