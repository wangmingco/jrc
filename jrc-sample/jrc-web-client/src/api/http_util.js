import request1 from '@/utils/request'

export function httpPost(params, request_url) {
  return request1({
    url: request_url,
    method: 'post',
    params
  })
}

export function post(params, request_url) {
  return request1({
    url: request_url,
    method: 'post',
    headers: {
      'Content-Type': 'application/json'
    },
    data: params
  })
}
