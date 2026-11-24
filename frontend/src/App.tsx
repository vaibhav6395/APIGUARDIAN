import { FormEvent, useEffect, useState } from 'react';
import { api } from './api';
import { ShieldCheck, LogOut, Plus, RefreshCw, FolderKanban, KeyRound, Activity, AlertTriangle, CheckCircle2 } from 'lucide-react';

type User={id:number;email:string;displayName:string};
type Project={id:number;workspaceId:number;name:string;description:string|null;createdAt:string;updatedAt:string};
type Policy={id:number;projectId:number;role:string;resource:string;action:string;expected:'ALLOW'|'DENY'};
type Finding={id:number;title:string;severity:string;status:string;details:string;createdAt:string};
type RunSummary={id:number;totalTests:number;passedTests:number;failedTests:number;status:string;createdAt:string};
type EvaluationHistory={id:number;role:string;resource:string;action:string;expected:'ALLOW'|'DENY';observed:'ALLOW'|'DENY';matched:boolean;explanation:string;createdAt:string};
type Evaluation={role:string;resource:string;action:string;expected:'ALLOW'|'DENY';observed:'ALLOW'|'DENY';matched:boolean;explanation:string};
type Session={accessToken:string;workspaceId:number;user:User};
export default function App(){
 const [session,setSession]=useState<Session|null>(null);
 const [register,setRegister]=useState(false);
 const [busy,setBusy]=useState(false);
 const [error,setError]=useState('');
 const [notice,setNotice]=useState('');
 const [email,setEmail]=useState('');
 const [password,setPassword]=useState('');
 const [displayName,setDisplayName]=useState('');
 const [workspaceName,setWorkspaceName]=useState('');
 const [projects,setProjects]=useState<Project[]>([]);
 const [projectId,setProjectId]=useState<number|null>(null);
 const [projectName,setProjectName]=useState('');
 const [projectDescription,setProjectDescription]=useState('');
 const [policies,setPolicies]=useState<Policy[]>([]);
 const [findings,setFindings]=useState<Finding[]>([]);
 const [history,setHistory]=useState<EvaluationHistory[]>([]);
 const [runs,setRuns]=useState<RunSummary[]>([]);
 const [role,setRole]=useState('USER');
 const [resource,setResource]=useState('orders:own');
 const [action,setAction]=useState('READ');
 const [expected,setExpected]=useState<'ALLOW'|'DENY'>('ALLOW');
 const [observed,setObserved]=useState<'ALLOW'|'DENY'>('ALLOW');
 const [evaluation,setEvaluation]=useState<Evaluation|null>(null);

 async function authenticate(e:FormEvent){
  e.preventDefault();setBusy(true);setError('');
  try{
   const body=register?{email,password,displayName,workspaceName}:{email,password};
   const result=await api<Omit<Session,'user'>&{user:User}>('/api/v1/auth/'+(register?'register':'login'),undefined,{method:'POST',body:JSON.stringify(body)});
   setSession(result);setNotice(register?'Workspace created successfully.':'Signed in successfully.');
   setPassword('');
  }catch(err){setError(err instanceof Error?err.message:'Authentication failed');}
  finally{setBusy(false);}
 }
 async function loadProjects(){
  if(!session)return;
  try{
   const list=await api<Project[]>(`/api/v1/workspaces/${session.workspaceId}/projects`,session.accessToken);
   setProjects(list);setProjectId(current=>current&&list.some(p=>p.id===current)?current:(list[0]?.id??null));
  }catch(err){setError(err instanceof Error?err.message:'Could not load projects');}
 }
 useEffect(()=>{if(session)void loadProjects();},[session]);
 async function loadPolicies(id:number){
  if(!session)return;
  try{setPolicies(await api<Policy[]>(`/api/v1/projects/${id}/policies`,session.accessToken));}
  catch(err){setError(err instanceof Error?err.message:'Could not load policies');}
 }
 useEffect(()=>{if(session&&projectId){void loadPolicies(projectId);void loadFindings(projectId);void loadHistory(projectId);void loadRuns(projectId);}else{setPolicies([]);setFindings([]);setHistory([]);setRuns([]);}},[session,projectId]);
 async function loadRuns(id:number){
  if(!session)return;
  try{setRuns(await api<RunSummary[]>(`/api/v1/projects/${id}/runs`,session.accessToken));}
  catch(err){setError(err instanceof Error?err.message:'Could not load test runs');}
 }
 async function runDemoSuite(){
  if(!session||!projectId)return;setBusy(true);setError('');
  try{await api<RunSummary>(`/api/v1/projects/${projectId}/runs/demo`,session.accessToken,{method:'POST'});await loadRuns(projectId);await loadFindings(projectId);await loadHistory(projectId);setNotice('Bundled demo checks completed. Review the run and findings below.');}
  catch(err){setError(err instanceof Error?err.message:'Demo checks failed');}
  finally{setBusy(false);}
 }
 async function loadHistory(id:number){
  if(!session)return;
  try{setHistory(await api<EvaluationHistory[]>(`/api/v1/projects/${id}/policies/evaluations`,session.accessToken));}
  catch(err){setError(err instanceof Error?err.message:'Could not load evaluation history');}
 }
 async function loadFindings(id:number){
  if(!session)return;
  try{setFindings(await api<Finding[]>(`/api/v1/projects/${id}/findings`,session.accessToken));}
  catch(err){setError(err instanceof Error?err.message:'Could not load findings');}
 }
 async function updateFinding(id:number,status:'OPEN'|'ACKNOWLEDGED'|'RESOLVED'){
  if(!session||!projectId)return;
  try{await api<Finding>(`/api/v1/projects/${projectId}/findings/${id}`,session.accessToken,{method:'PATCH',body:JSON.stringify({status})});await loadFindings(projectId);}
  catch(err){setError(err instanceof Error?err.message:'Could not update finding');}
 }
 async function createProject(e:FormEvent){
  e.preventDefault();if(!session)return;setBusy(true);setError('');
  try{
   const project=await api<Project>(`/api/v1/workspaces/${session.workspaceId}/projects`,session.accessToken,{method:'POST',body:JSON.stringify({name:projectName,description:projectDescription})});
   setProjects(current=>[project,...current]);setProjectId(project.id);setProjectName('');setProjectDescription('');setNotice('Project created.');
  }catch(err){setError(err instanceof Error?err.message:'Could not create project');}
  finally{setBusy(false);}
 }
 async function savePolicy(e:FormEvent){
  e.preventDefault();if(!session||!projectId)return;setBusy(true);setError('');
  try{
   await api<Policy>(`/api/v1/projects/${projectId}/policies`,session.accessToken,{method:'POST',body:JSON.stringify({role,resource,action,expected})});
   await loadPolicies(projectId);setNotice('Authorization policy saved.');
  }catch(err){setError(err instanceof Error?err.message:'Could not save policy');}
  finally{setBusy(false);}
 }
 async function runEvaluation(e:FormEvent){
  e.preventDefault();if(!session||!projectId)return;setBusy(true);setError('');setEvaluation(null);
  try{
   const result=await api<Evaluation>(`/api/v1/projects/${projectId}/policies/evaluate`,session.accessToken,{method:'POST',body:JSON.stringify({rule:{role,resource,action,expected},observed})});
   setEvaluation(result);await loadFindings(projectId);await loadHistory(projectId);
  }catch(err){setError(err instanceof Error?err.message:'Evaluation failed');}
  finally{setBusy(false);}
 }
 if(!session)return <main className="authPage"><div className="authCard"><div className="authBrand"><ShieldCheck size={30}/><div><b>APIGUARDIAN</b><small>API AUTHORIZATION SECURITY</small></div></div><span className="eyebrow">DEFENSIVE SECURITY WORKSPACE</span><h1>{register?'Create your workspace':'Welcome back'}</h1><p className="authLead">Define access expectations and verify authorization decisions in a controlled environment.</p>{error&&<div className="alert error"><AlertTriangle size={16}/>{error}</div>}<form onSubmit={authenticate} className="authForm"><label>Email address<input type="email" value={email} onChange={e=>setEmail(e.target.value)} required maxLength={254} autoComplete="email"/></label><label>Password<input type="password" value={password} onChange={e=>setPassword(e.target.value)} required minLength={register?12:1} maxLength={72} autoComplete={register?'new-password':'current-password'}/>{register&&<small>Use at least 12 characters.</small>}</label>{register&&<><label>Your name<input value={displayName} onChange={e=>setDisplayName(e.target.value)} required maxLength={100}/></label><label>Workspace name<input value={workspaceName} onChange={e=>setWorkspaceName(e.target.value)} required maxLength={120}/></label></>}<button className="primary authSubmit" disabled={busy}>{busy?'Please wait…':register?'Create workspace':'Sign in'}</button></form><p className="switchAuth">{register?'Already have an account?':'New to APIGUARDIAN?'} <button onClick={()=>{setRegister(!register);setError('');}}> {register?'Sign in':'Create an account'}</button></p><p className="authFoot">Only assess APIs you own or have explicit permission to test.</p></div></main>;
 return <div className="appShell"><aside className="appSide"><div className="authBrand"><ShieldCheck size={28}/><div><b>APIGUARDIAN</b><small>AUTHORIZATION SECURITY</small></div></div><div className="sideWorkspace"><small>WORKSPACE</small><b>{session.user.displayName}</b><span>{session.user.email}</span></div><div className="sideLabel">CONSOLE</div><div className="sideNav active"><Activity size={17}/> Overview</div><div className="sideNav"><FolderKanban size={17}/> Projects</div><div className="sideNav"><KeyRound size={17}/> Policy testing</div><div className="sideBottom"><span className="statusDot"/> Authenticated session<button title="Sign out" onClick={()=>{setSession(null);setProjects([]);setPolicies([]);setProjectId(null);}}><LogOut size={16}/></button></div></aside>
 <main className="appMain"><header className="appHeader"><div><div className="eyebrow">SECURITY WORKSPACE / OVERVIEW</div><h1>Authorization workspace</h1><p>Manage projects and evaluate expected access decisions.</p></div><div className="headerRight"><span className="env">AUTHENTICATED</span><button className="iconButton" onClick={()=>void loadProjects()} title="Refresh projects"><RefreshCw size={17}/></button></div></header>
 {error&&<div className="alert error"><AlertTriangle size={16}/>{error}</div>}{notice&&<div className="alert success"><CheckCircle2 size={16}/>{notice}<button onClick={()=>setNotice('')}>×</button></div>}
 <section className="heroPanel"><div><span className="pill">SECURE WORKSPACE</span><h2>Make authorization<br/><span>measurable.</span></h2><p>Record the access each role should have, then compare it with an observed decision. Start with an API project you are authorized to assess.</p></div><div className="heroIcon"><ShieldCheck size={66}/></div></section>
 <section className="statGrid"><div className="statCard"><small>Projects</small><strong>{projects.length}</strong><span>In your workspace</span></div><div className="statCard"><small>Policies</small><strong>{policies.length}</strong><span>For selected project</span></div><div className="statCard"><small>Evaluation</small><strong>{evaluation?(evaluation.matched?'PASS':'MISMATCH'):'—'}</strong><span>Latest in this session</span></div></section>
 <section className="panelBlock"><div className="blockHead"><div><h2>Projects</h2><p>Create and select a project to manage its authorization policies.</p></div></div><form className="createProject" onSubmit={createProject}><input placeholder="Project name" value={projectName} onChange={e=>setProjectName(e.target.value)} required maxLength={120}/><input placeholder="Description (optional)" value={projectDescription} onChange={e=>setProjectDescription(e.target.value)} maxLength={1000}/><button className="primary" disabled={busy}><Plus size={15}/> Add project</button></form>{projects.length===0?<p className="emptyState">No projects yet. Create your first API project above.</p>:<div className="projectList">{projects.map(p=><button key={p.id} className={projectId===p.id?'projectItem selected':'projectItem'} onClick={()=>{setProjectId(p.id);setEvaluation(null);}}><FolderKanban size={18}/><span><b>{p.name}</b><small>{p.description||'No description'}</small></span><small>#{p.id}</small></button>)}</div>}</section>
 {projectId&&<section className="twoCol"><div className="panelBlock"><div className="blockHead"><div><h2>Authorization policies</h2><p>Policies are stored against the selected project.</p></div></div><form className="policyForm" onSubmit={savePolicy}><label>Role<input value={role} onChange={e=>setRole(e.target.value)} required maxLength={80}/></label><label>Resource<input value={resource} onChange={e=>setResource(e.target.value)} required maxLength={300}/></label><label>Action<input value={action} onChange={e=>setAction(e.target.value)} required maxLength={30}/></label><label>Expected decision<select value={expected} onChange={e=>setExpected(e.target.value as 'ALLOW'|'DENY')}><option>ALLOW</option><option>DENY</option></select></label><button className="primary" disabled={busy}>Save policy</button></form><div className="policyList">{policies.map(p=><div className="policyRow" key={p.id}><span><b>{p.role}</b><small>{p.action} · {p.resource}</small></span><em className={p.expected==='ALLOW'?'allow':'deny'}>{p.expected}</em></div>)}{policies.length===0&&<p className="emptyState">No policies saved for this project.</p>}</div></div>
 <div className="panelBlock"><div className="blockHead"><div><h2>Evaluate a decision</h2><p>Compare expected and observed authorization outcomes.</p></div></div><form className="policyForm" onSubmit={runEvaluation}><label>Role<input value={role} onChange={e=>setRole(e.target.value)} required/></label><label>Resource<input value={resource} onChange={e=>setResource(e.target.value)} required/></label><label>Action<input value={action} onChange={e=>setAction(e.target.value)} required/></label><label>Observed decision<select value={observed} onChange={e=>setObserved(e.target.value as 'ALLOW'|'DENY')}><option>ALLOW</option><option>DENY</option></select></label><button className="primary" disabled={busy}>Evaluate policy</button></form>{evaluation&&<div className={evaluation.matched?'resultBox pass':'resultBox fail'}><div>{evaluation.matched?<CheckCircle2 size={20}/>:<AlertTriangle size={20}/>}<b>{evaluation.matched?'Policy matched':'Authorization mismatch'}</b></div><p>{evaluation.explanation}</p><small>Expected: {evaluation.expected} · Observed: {evaluation.observed}</small></div>}</div></section>}
 {projectId&&<section className="panelBlock"><div className="blockHead"><div><h2>Local demo test runs</h2><p>Runs a fixed synthetic authorization suite; it does not contact external systems.</p></div><button className="primary" onClick={()=>void runDemoSuite()} disabled={busy}>Run demo checks</button></div>{runs.length===0?<p className="emptyState">No demo runs yet. Save policies and run the local suite.</p>:runs.map(run=><div className="historyRow" key={run.id}><span className={run.failedTests===0?'historyPass':'historyFail'}>{run.failedTests===0?'PASS':'ISSUES'}</span><span><b>Run #{run.id}</b><small>{run.passedTests} passed · {run.failedTests} mismatched · {run.totalTests} total</small></span><time>{new Date(run.createdAt).toLocaleString()}</time></div>)}</section>}
 {projectId&&<section className="panelBlock"><div className="blockHead"><div><h2>Evaluation history</h2><p>Recent authorization decisions recorded for this project.</p></div></div>{history.length===0?<p className="emptyState">No evaluations have been recorded yet.</p>:history.map(item=><div className="historyRow" key={item.id}><span className={item.matched?'historyPass':'historyFail'}>{item.matched?'MATCH':'MISMATCH'}</span><span><b>{item.role} · {item.action} {item.resource}</b><small>Expected {item.expected} · observed {item.observed}</small></span><time>{new Date(item.createdAt).toLocaleString()}</time></div>)}</section>}
 {projectId&&<section className="panelBlock"><div className="blockHead"><div><h2>Findings</h2><p>Authorization mismatches recorded from evaluations.</p></div></div>{findings.length===0?<p className="emptyState">No findings for this project. Run an evaluation with a mismatch to create one.</p>:findings.map(f=><div className="findingItem" key={f.id}><span className={`severityTag ${f.severity.toLowerCase()}`}>{f.severity}</span><span className="findingInfo"><b>{f.title}</b><small>{f.details}</small><small>Status: {f.status}</small></span><span className="findingActions">{f.status!=='ACKNOWLEDGED'&&f.status!=='RESOLVED'&&<button onClick={()=>void updateFinding(f.id,'ACKNOWLEDGED')}>Acknowledge</button>}{f.status!=='RESOLVED'&&<button onClick={()=>void updateFinding(f.id,'RESOLVED')}>Resolve</button>}</span></div>)}</section>}
 <footer>APIGUARDIAN <span>DEFENSIVE API AUTHORIZATION TESTING</span><span>v0.2 · EARLY ACCESS</span></footer></main></div>;
}
