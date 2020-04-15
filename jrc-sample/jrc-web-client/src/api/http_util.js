import request1 from '@/utils/request'

export function httpParamPost(params, request_url) {
  return request1({
    url: request_url,
    method: 'post',
    params
  })
}

export function httpDataPost(params, request_url) {
  return request1({
    url: request_url,
    method: 'post',
    headers: {
      'Content-Type': 'application/json'
    },
    data: params
  })
}
