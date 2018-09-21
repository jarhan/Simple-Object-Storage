import requests
import hashlib

BASE_URL = 'http://127.0.0.1:8080'
STATUS_OK = requests.codes['ok']

# def test_get_status():
#     """GET /status should have status_code 200"""
#     resp = requests.get(BASE_URL + '/all')
#     assert resp.status_code == STATUS_OK

# def test_create_delete_bucket():

# 	resp = requests.post(BASE_URL + '/buckettest?create')
# 	assert resp.status_code == STATUS_OK

# 	resp_delete = requests.delete(BASE_URL + '/buckettest?delete')
# 	assert resp_delete.status_code == STATUS_OK


def test_uploading():
	# resp = requests.post(BASE_URL + '/buckettest?create')
	# assert resp.status_code == STATUS_OK

	# resp = requests.post(BASE_URL+'/buckettest/objecttest?create')
	# assert resp.status_code == STATUS_OK

	# data = open('./test2.jpg', 'rb').read()
	# resp = requests.put(url=BASE_URL+'/bb/n?partNumber=2',
	# 					data=data,
	# 					headers={ 'Content-MD5': hashlib.md5(data).hexdigest()})
	#
	# assert resp.status_code == STATUS_OK
	data = open('./t3.txt', 'rb').read()
	resp = requests.put(url=BASE_URL+'/t1/t3?partNumber=1',
						data=data,
						headers={ 'Content-MD5': hashlib.md5(data).hexdigest()})

	assert resp.status_code == STATUS_OK

# def test_get_status():
#     """GET /status should have status_code 200"""
#     resp = requests.delete(BASE_URL + '/deleteall')
#     assert resp.status_code == STATUS_OK

# def test_addMetadata():
# 	# resp = requests.post(BASE_URL + '/buckettest?create')
# 	# assert resp.status_code == STATUS_OK
#
# 	# resp = requests.post(BASE_URL+'/buckettest/objecttest?create')
# 	# assert resp.status_code == STATUS_OK
#
# 	resp = requests.put(url=BASE_URL+'/b/c?metadata&key=dataSource',
# 						data="http://www.ietf.org/rfc/rfc2616.txt")
#
# 	assert resp.status_code == 200
