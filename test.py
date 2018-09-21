import requests
import hashlib

BASE_URL = 'http://127.0.0.1:8080'
STATUS_OK = requests.codes['ok']
STATUS_BAD_REQUEST = 400
STATUS_NOT_FOUND = 404

# def test_get_status():
#     """GET /status should have status_code 200"""
#     resp = requests.get(BASE_URL + '/all')
#     assert resp.status_code == STATUS_OK

def test_bucket():

	# create check
	resp = requests.get(BASE_URL + '/buckettest?create')
	assert resp.status_code == STATUS_BAD_REQUEST

	resp = requests.delete(BASE_URL + '/buckettest?create')
	assert resp.status_code == STATUS_BAD_REQUEST

	resp = requests.post(BASE_URL + '/buckettest?creae')
	assert resp.status_code == STATUS_BAD_REQUEST

	resp = requests.post(BASE_URL + '/buckettest?create')
	assert resp.status_code == STATUS_OK

	resp = requests.post(BASE_URL + '/another_bucket?create')
	assert resp.status_code == STATUS_OK

	resp = requests.post(BASE_URL + '/another-bucket?create')
	assert resp.status_code == STATUS_OK

	resp = requests.post(BASE_URL + '/bucket?create')
	assert resp.status_code == STATUS_OK

	resp = requests.post(BASE_URL + '/another-bucket?create')
	assert resp.status_code == STATUS_BAD_REQUEST

	resp = requests.post(BASE_URL + '/_another-another_bucket?create')
	assert resp.status_code == STATUS_OK

	# delete check
	resp_delete = requests.delete(BASE_URL + '/buckettest?delet')
	assert resp_delete.status_code == STATUS_BAD_REQUEST

	resp_delete = requests.post(BASE_URL + '/buckettest?delete')
	assert resp_delete.status_code == STATUS_BAD_REQUEST

	resp_delete = requests.delete(BASE_URL + '/no_bucket?delete')
	assert resp_delete.status_code == STATUS_BAD_REQUEST

	resp_delete = requests.delete(BASE_URL + '/buckettest?delete')
	assert resp_delete.status_code == STATUS_OK

	# list check
	resp = requests.get(BASE_URL + '/another_bucket?list')
	assert resp.status_code == STATUS_OK

	resp = requests.get(BASE_URL + '/no_bucket?list')
	assert resp.status_code == STATUS_BAD_REQUEST


def test_uploading():
	# create object check
	resp = requests.post(BASE_URL + '/bucket/object?create')
	assert resp.status_code == STATUS_OK

	resp = requests.post(BASE_URL + '/another_bucket/another_object?create')
	assert resp.status_code == STATUS_OK

	resp = requests.post(BASE_URL + '/another-bucket/another-object?create')
	assert resp.status_code == STATUS_OK

	resp = requests.post(BASE_URL + '/bucket/object?create')
	assert resp.status_code == STATUS_BAD_REQUEST

	resp = requests.post(BASE_URL + '/no_bucket/no_object?create')
	assert resp.status_code == STATUS_BAD_REQUEST

	# upload part check
	data = open('./t1.txt', 'rb').read()
	resp = requests.put(url=BASE_URL+'/bucket/object?partNumber=3',
						data=data,
						headers={ 'Content-MD5': hashlib.md5(data).hexdigest()})

	assert resp.status_code == STATUS_OK

	data = open('./t2.txt', 'rb').read()
	resp = requests.put(url=BASE_URL+'/bucket/object?partNumber=5',
						data=data,
						headers={ 'Content-MD5': hashlib.md5(data).hexdigest()})

	assert resp.status_code == STATUS_OK

	data = open('./t3.txt', 'rb').read()
	resp = requests.put(url=BASE_URL+'/bucket/object?partNumber=10',
						data=data,
						headers={ 'Content-MD5': hashlib.md5(data).hexdigest()})

	assert resp.status_code == STATUS_OK

	data = open('./t3.txt', 'rb').read()
	resp = requests.put(url=BASE_URL+'/bucket/object?partNumber=1',
						data=data,
						headers={ 'Content-MD5': hashlib.md5(data).hexdigest()})

	assert resp.status_code == STATUS_OK

	data = open('./t2.txt', 'rb').read()
	resp = requests.put(url=BASE_URL+'/bucket/object?partNumber=1',
						data=data,
						headers={ 'Content-MD5': hashlib.md5(data).hexdigest()})

	assert resp.status_code == STATUS_OK

	data = open('./t2.txt', 'rb').read()
	resp = requests.put(url=BASE_URL+'/bucket/obJect?partNumber=20',
						data=data,
						headers={ 'Content-MD5': hashlib.md5(data).hexdigest()})

	assert resp.status_code == STATUS_OK

	data = open('./t3.txt', 'rb').read()
	resp = requests.put(url=BASE_URL+'/another-bucket/another-object?partNumber=1',
						data=data,
						headers={ 'Content-MD5': hashlib.md5(data).hexdigest()})

	assert resp.status_code == STATUS_OK

	data = open('./t3.txt', 'rb').read()
	resp = requests.put(url=BASE_URL+'/bucket/no_object?partNumber=1',
						data=data,
						headers={ 'Content-MD5': hashlib.md5(data).hexdigest()})

	assert resp.status_code == STATUS_BAD_REQUEST

	data = open('./t1.txt', 'rb').read()
	resp = requests.put(url=BASE_URL+'/bucket/object?partNumber=10001',
						data=data,
						headers={ 'Content-MD5': hashlib.md5(data).hexdigest()})

	assert resp.status_code == STATUS_BAD_REQUEST

	data = open('./t3.txt', 'rb').read()
	resp = requests.put(url=BASE_URL+'/bucket/object?partNumber=dsfk',
						data=data,
						headers={ 'Content-MD5': hashlib.md5(data).hexdigest()})

	assert resp.status_code == STATUS_BAD_REQUEST

	data = open('./t3.txt', 'rb').read()
	resp = requests.put(url=BASE_URL+'/no_bucket/object?partNumber=1',
						data=data,
						headers={ 'Content-MD5': hashlib.md5(data).hexdigest()})

	assert resp.status_code == STATUS_BAD_REQUEST

	data = open('./t3.txt', 'rb').read()
	resp = requests.put(url=BASE_URL+'/bucket/no_object?partNumber=1',
						data=data,
						headers={ 'Content-MD5': hashlib.md5(data).hexdigest()})

	assert resp.status_code == STATUS_BAD_REQUEST

	# complete check
	resp = requests.post(BASE_URL + '/bucket/object?complete')
	assert resp.status_code == STATUS_OK

	resp = requests.get(BASE_URL + '/another_bucket/another_object?complete')
	assert resp.status_code == STATUS_BAD_REQUEST

	resp = requests.post(BASE_URL + '/no_bucket/object?complete')
	assert resp.status_code == STATUS_BAD_REQUEST

	resp = requests.post(BASE_URL + '/bucket/no_object?complete')
	assert resp.status_code == STATUS_BAD_REQUEST

	# upload after complete
	data = open('./t3.txt', 'rb').read()
	resp = requests.put(url=BASE_URL+'/bucket/object?partNumber=1',
						data=data,
						headers={ 'Content-MD5': hashlib.md5(data).hexdigest()})

	assert resp.status_code == STATUS_BAD_REQUEST

	# delete part
	resp = requests.delete(BASE_URL + '/another-bucket/another-object?partNumber=1')
	assert resp.status_code == STATUS_OK

	resp = requests.delete(BASE_URL + '/another-bucket/another-object?partNumber=1')
	assert resp.status_code == STATUS_BAD_REQUEST

	resp = requests.delete(BASE_URL + '/no_bucket/object?partNumber=1')
	assert resp.status_code == STATUS_BAD_REQUEST

	resp = requests.delete(BASE_URL + '/bucket/no_object?partNumber=1')
	assert resp.status_code == STATUS_BAD_REQUEST

	# delete object
	resp = requests.delete(BASE_URL + '/another-bucket/another-object?delete')
	assert resp.status_code == STATUS_OK

	resp = requests.delete(BASE_URL + '/no_bucket/object?delete')
	assert resp.status_code == STATUS_BAD_REQUEST

	resp = requests.delete(BASE_URL + '/bucket/no_object?delete')
	assert resp.status_code == STATUS_BAD_REQUEST

	# add and update metadata
	resp = requests.put(url=BASE_URL+'/bucket/object?metadata&key=dataSource',
						data="http://www.ietf.org/rfc/rfc2616.txt")

	assert resp.status_code == STATUS_OK

	resp = requests.put(url=BASE_URL+'/bucket/object?metadata&key=license',
						data="Not this")

	assert resp.status_code == STATUS_OK

	resp = requests.put(url=BASE_URL+'/bucket/object?metadata&key=license',
						data="Apache 2.0")

	assert resp.status_code == STATUS_OK

	resp = requests.put(url=BASE_URL+'/bucket/object?metadata&key=forDelete',
						data="delete it")

	assert resp.status_code == STATUS_OK

	resp = requests.put(url=BASE_URL+'/no_bucket/object?metadata&key=license',
						data="Apache 2.0")

	assert resp.status_code == STATUS_NOT_FOUND

	resp = requests.put(url=BASE_URL+'/bucket/no_object?metadata&key=license',
						data="Apache 2.0")

	assert resp.status_code == STATUS_NOT_FOUND

	# delete metadata
	resp = requests.delete(url=BASE_URL+'/bucket/object?metadata&key=forDelete')
	assert resp.status_code == STATUS_OK

	resp = requests.delete(url=BASE_URL+'/bucket/object?metadata&key=forDelete')
	assert resp.status_code == STATUS_OK

	resp = requests.delete(url=BASE_URL+'/no_bucket/object?metadata&key=license')
	assert resp.status_code == STATUS_NOT_FOUND

	resp = requests.delete(url=BASE_URL+'/bucket/no_object?metadata&key=license')
	assert resp.status_code == STATUS_NOT_FOUND

	# get metadata
	resp = requests.get(url=BASE_URL+'/bucket/object?metadata&key=license')
	assert resp.status_code == STATUS_OK

	resp = requests.get(url=BASE_URL+'/bucket/object?metadata&key=forDelete')
	assert resp.status_code == STATUS_OK

	resp = requests.get(url=BASE_URL+'/no_bucket/object?metadata&key=license')
	assert resp.status_code == STATUS_NOT_FOUND

	resp = requests.get(url=BASE_URL+'/bucket/no_object?metadata&key=license')
	assert resp.status_code == STATUS_NOT_FOUND

	resp = requests.get(url=BASE_URL+'/bucket/object?metadata')
	assert resp.status_code == STATUS_OK

	resp = requests.get(url=BASE_URL+'/another_bucket/another_object?metadata')
	assert resp.status_code == STATUS_OK

	resp = requests.get(url=BASE_URL+'/no_bucket/object?metadata')
	assert resp.status_code == STATUS_NOT_FOUND

	resp = requests.get(url=BASE_URL+'/bucket/no_object?metadata')
	assert resp.status_code == STATUS_NOT_FOUND
