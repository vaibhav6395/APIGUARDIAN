export async function api<T>(path:string,token:string|undefined,options:RequestInit={}):Promise<T>{
 const headers=new Headers(options.headers);
 if(options.body!==undefined&&!headers.has('Content-Type'))headers.set('Content-Type','application/json');
 if(token)headers.set('Authorization',`Bearer ${token}`);
 const response=await fetch(path,{...options,headers});
 const data=await response.json().catch(()=>({message:'Unexpected server response'}));
 if(!response.ok)throw new Error(data.message||data.error||`Request failed (${response.status})`);
 return data as T;
}
