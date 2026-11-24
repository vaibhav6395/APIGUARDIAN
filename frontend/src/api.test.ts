import {afterEach,describe,expect,it,vi} from 'vitest';
import {api} from './api';

afterEach(()=>vi.unstubAllGlobals());

describe('api request helper',()=>{
 it('sends JSON and attaches bearer credentials',async()=>{
  const fetchMock=vi.fn().mockResolvedValue(new Response(JSON.stringify({ok:true}),{status:200,headers:{'Content-Type':'application/json'}}));
  vi.stubGlobal('fetch',fetchMock);
  const result=await api<{ok:boolean}>('/api/v1/projects','test-token',{method:'POST',body:JSON.stringify({name:'demo'})});
  expect(result.ok).toBe(true);
  const request=fetchMock.mock.calls[0][1] as RequestInit;
  const headers=new Headers(request.headers);
  expect(headers.get('Authorization')).toBe('Bearer test-token');
  expect(headers.get('Content-Type')).toBe('application/json');
 });
 it('surfaces a safe API error message',async()=>{
  vi.stubGlobal('fetch',vi.fn().mockResolvedValue(new Response(JSON.stringify({message:'Invalid credentials'}),{status:401,headers:{'Content-Type':'application/json'}})));
  await expect(api('/api/v1/auth/login',undefined,{method:'POST',body:'{}'})).rejects.toThrow('Invalid credentials');
 });
});
