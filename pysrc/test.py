#coding=utf-8
import urllib
import urllib2
import json
import requests
from time import *

gurl = "http://127.0.0.1:8080"

def test_login_success(url, username, password):
    data = {"username": username, "password": password}
    res = requests.post(
        url + "/login",
        data=json.dumps(data),
        headers={"Content-type": "application/json"},
    )

    assert res.status_code == 200

def test_login_error_password(url, username):
    data = {"username": username, "password": "nottherightpassword"}
    res = requests.post(
        url + "/login",
        data=json.dumps(data),
        headers={"Content-type": "application/json"},
    )

    assert res.status_code == 403
    assert res.json().get("code") == "USER_AUTH_FAIL"
    assert res.json().get("message") == u"用户名或密码错误"


def test_login_post_data(url, username, password):
    data = {"username": username, "password": password}
    res = requests.post(
        url + "/login",
        data=data,
        headers={"Content-type": "application/json"},
    )

    assert res.status_code == 400
    assert res.json().get("code") == "MALFORMED_JSON"
    assert res.json().get("message") == u"格式错误"

def post(url, data):
    print url
    req = urllib2.Request(url)
    req.add_header('Content-Type', 'application/json')
    opener = urllib2.build_opener(urllib2.HTTPCookieProcessor())
    response = opener.open(req, data)
    res = response.read()
    return res

def testlogin():
    posturl = "http://127.0.0.1:8080/login"
    msg = {}
    msg["username"] = "root"
    msg["password"] = "toor"
    res = post(posturl,json.dumps(msg, ensure_ascii = False))
    print res

def testfood():
    res = requests.get(gurl + "/foods", headers={"Access-Token": "userroo"})
    print res

def testcarts():
    posturl = "http://127.0.0.1:8080/carts?access_token=userroot"
    msg = {}
    res = post(posturl,json.dumps(msg))
    print res

def testaddFood():
    posturl = "http://127.0.0.1:8080/carts/abcd?access_token=userroot"
    msg = {}
    res = requests.patch(posturl,json.dumps(msg))
    print res

testfood()
