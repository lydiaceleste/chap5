package Semant;
import Translate.Exp;
import Types.Type;


public class Semant {
  Env env;
  public Semant(ErrorMsg.ErrorMsg err) {
    this(new Env(err));
  }
  Semant(Env e) {
    env = e;
  }

  public void transProg(Absyn.Exp exp) {
    transExp(exp);
  }

  private void error(int pos, String msg) {
    env.errorMsg.error(pos, msg);
  }

  static final Types.VOID   VOID   = new Types.VOID();
  static final Types.INT    INT    = new Types.INT();
  static final Types.STRING STRING = new Types.STRING();
  static final Types.NIL    NIL    = new Types.NIL();


  private Exp checkInt(ExpTy et, int pos) {
    if (!INT.coerceTo(et.ty))
      error(pos, "integer required, not found");
    return et.exp;
  }

  private Exp compCheck(ExpTy et, int pos){
    Type type = et.ty.actual();
    if (type instanceof Types.RECORD || type instanceof Types.ARRAY) {
      error(pos, "Cannot compare records and arrays");
    } else if (type != INT && type != STRING && type != NIL) {
      error(pos, "Types are not comparable :(");
    }
    return et.exp;
  }

  ExpTy transExp(Absyn.Exp e) {
    ExpTy result;
    if (e == null)
      return new ExpTy(null, VOID);
    else if (e instanceof Absyn.OpExp)
      result = transExp((Absyn.OpExp)e);
    else if (e instanceof Absyn.LetExp)
      result = transExp((Absyn.LetExp)e);
    else if (e instanceof Absyn.ArrayExp)
      result = transExp((Absyn.ArrayExp)e);
    else if (e instanceof Absyn.AssignExp)
      result = transExp((Absyn.AssignExp)e);
    else if (e instanceof Absyn.BreakExp)
      result = transExp((Absyn.BreakExp)e);    
    else if (e instanceof Absyn.CallExp)
      result = transExp((Absyn.CallExp)e);
    else if (e instanceof Absyn.ForExp)
      result = transExp((Absyn.ForExp)e);
    else if (e instanceof Absyn.IfExp)
      result = transExp((Absyn.IfExp)e);
    else if (e instanceof Absyn.NilExp)
      result = transExp((Absyn.NilExp)e);
    else if (e instanceof Absyn.RecordExp)
      result = transExp((Absyn.RecordExp)e);
    else if (e instanceof Absyn.SeqExp)
      result = transExp((Absyn.SeqExp)e);
    else if (e instanceof Absyn.StringExp)
      result = transExp((Absyn.StringExp)e);
    else if (e instanceof Absyn.IntExp)
        result = transExp((Absyn.IntExp)e);   
    else if (e instanceof Absyn.VarExp)
       result = transExp((Absyn.VarExp)e);  
     else if (e instanceof Absyn.WhileExp)
       result = transExp((Absyn.WhileExp)e);    
    else throw new Error("Semant.transExp");
    e.type = result.ty;
    return result;
  }

  ExpTy transExp(Absyn.OpExp e) {
    ExpTy left = transExp(e.left);
    ExpTy right = transExp(e.right);

    switch (e.oper) {
    case Absyn.OpExp.PLUS:
      checkInt(left, e.left.pos);
      checkInt(right, e.right.pos);
      return new ExpTy(null, INT);
    case Absyn.OpExp.MINUS:
      checkInt(left, e.left.pos);
      checkInt(right, e.right.pos);
      return new ExpTy(null, INT);
    case Absyn.OpExp.MUL:
      checkInt(left, e.left.pos);
      checkInt(right, e.right.pos);
      return new ExpTy(null, INT);      
    case Absyn.OpExp.DIV:
      checkInt(left, e.left.pos);
      checkInt(right, e.right.pos);
      return new ExpTy(null, INT);
    case Absyn.OpExp.LT:
    case Absyn.OpExp.LE:
    case Absyn.OpExp.GT:
    case Absyn.OpExp.GE:
    case Absyn.OpExp.NE:
      compCheck(left, e.left.pos);
      compCheck(right, e.right.pos);
      if ((!left.ty.coerceTo(right.ty)) && (!right.ty.coerceTo(left.ty))){
        error(e.pos, "Operands are incompatible.");
      }
      return new ExpTy(null, INT);
    case Absyn.OpExp.EQ:
      compCheck(left, e.left.pos);
      compCheck(right, e.right.pos);
      if ((!left.ty.coerceTo(right.ty)) && (!right.ty.coerceTo(left.ty))){
        error(e.pos, "Operands are incompatible.");
      }
      return new ExpTy(null, left.ty);
    default:
      throw new Error("Unknown operator");
    }
  }

  ExpTy transExp(Absyn.LetExp e) {
    env.venv.beginScope();
    env.tenv.beginScope();
    for (Absyn.DecList d = e.decs; d != null; d = d.tail) {
      transDec(d.head);
    }
    ExpTy body = transExp(e.body);
    env.venv.endScope();
    env.tenv.endScope();
    return new ExpTy(null, body.ty);
  }

  ExpTy transExp(Absyn.IfExp e) {
    ExpTy test = transExp(e.test);
    checkInt(test, e.test.pos);
    ExpTy thenclause = transExp(e.thenclause);
    ExpTy elseclause = null;
    if (e.elseclause != null) {
        elseclause = transExp(e.elseclause);
        if (!thenclause.ty.coerceTo(elseclause.ty) && !elseclause.ty.coerceTo(thenclause.ty)) {
            error(e.pos, "Type Mismatch.");
        }
    } else if (!thenclause.ty.coerceTo(VOID)) {
        error(e.pos, "Missing else-clause not VOID.");
      }
    return new ExpTy(null, thenclause.ty);
  }

  ExpTy transExp(Absyn.IntExp e) {
      return new ExpTy(null, INT);
  }

  ExpTy transExp(Absyn.NilExp e) {
    return new ExpTy(null, NIL);
  }

  ExpTy transExp(Absyn.StringExp e) {
    return new ExpTy(null, STRING);
  }

  ExpTy transExp(Absyn.SeqExp e) {
    ExpTy type = new ExpTy(null, VOID);
    for(Absyn.ExpList exp = e.list; exp != null; exp = exp.tail)
    {
      type = transExp(exp.head);
    }
    return type;
  }

  ExpTy transExp(Absyn.BreakExp e) {
    return new ExpTy(null, VOID);
  }

  ExpTy transExp(Absyn.WhileExp e) {
    Type test = transExp(e.test).ty;
    if (test != INT) {
        error(e.pos, "Test clause MUST be an int :(");
    }
    ExpTy body = transExp(e.body);
    if (body.ty != VOID) {
        error(e.pos, "While loop body MUST evaluate to void :(");
    }
    return new ExpTy(null, VOID);
  }

  ExpTy transExp(Absyn.AssignExp e) {
    ExpTy r = transVar(e.var);
    ExpTy l = transExp(e.exp);
    if (!l.ty.coerceTo(r.ty)) {
      error(e.pos, "Type Mismatch.");
    }
    return new ExpTy(null, VOID);
  }

  ExpTy transExp(Absyn.VarExp e) {
     return transVar(e.var);
  }

  ExpTy transExp(Absyn.RecordExp e) {
    if (e.fields == null){
      error(e.pos, "Unknown type");
      return null;
    }
    ExpTy init = transExp(e.fields.init);
    for(Absyn.FieldExpList f = e.fields.tail; f != null; f = f.tail)
    {
      transExp(f.init);
    }
    Types.RECORD recordType = (Types.RECORD)env.tenv.get(e.typ);
    return new ExpTy(init.exp, recordType);
  }

  ExpTy transExp(Absyn.ArrayExp e) {
    Type result = (Type)env.tenv.get(e.typ);
    if (result == null) {
        error(e.pos, "undefined type " + e.typ);
        return new ExpTy(null, VOID);
    } else if (!(result instanceof Types.ARRAY)) {
        error(e.pos, "array type expected");
        return new ExpTy(null, VOID);
    } else {
        Types.ARRAY at = (Types.ARRAY)result;
        ExpTy size = transExp(e.size);
        if (!size.ty.coerceTo(INT)) {
            error(e.size.pos, "array size must be an integer");
        }
        ExpTy init = transExp(e.init);
        if (!init.ty.coerceTo(at.element)) {
            error(e.init.pos, "array initialization type mismatch");
        }
        return new ExpTy(null, at);
    }
  }
  
  ExpTy transExp(Absyn.CallExp e) {
    Entry x = (Entry)env.venv.get(e.func);
    if (x == null) {
        error(e.pos, "Function " + e.func + " undefined");
        return new ExpTy(null, INT);
    } else if (!(x instanceof FunEntry)) {
        error(e.pos, e.func + " is not a function");
        return new ExpTy(null, INT);
    }
    FunEntry f = (FunEntry) x;
    Types.RECORD expected = f.formals;
    Absyn.ExpList args = e.args;
    int expectedCount = 0;
    while (expected != null) {
        expectedCount++;
        expected = expected.tail;
    }
    int actualCount = 0;
    while (args != null) {
        actualCount++;
        args = args.tail;
    }
    if (expectedCount != actualCount) {
        error(e.pos, "Function " + e.func + " expects " + expectedCount + " arguments, but " + actualCount + " were provided");
        return new ExpTy(null, INT);
    }
    int i = 0;
    expected = f.formals;
    args = e.args;
    while (expected != null && args != null) {
        ExpTy arg = transExp(args.head);
        if (!arg.ty.coerceTo(expected.fieldType)) {
            error(args.head.pos, "Parameter type mismatch in argument " + (i + 1));
        }
        expected = expected.tail;
        args = args.tail;
        i++;
    }
    return new ExpTy(null, f.result);
  }

  ExpTy transExp(Absyn.ForExp e) {
    env.venv.beginScope();
    Types.Type varTy = null;
    if (e.var instanceof Absyn.SimpleVar) {
        Absyn.SimpleVar var = (Absyn.SimpleVar)e.var;
        VarEntry varEntry = (VarEntry)env.venv.get(var.name);
        varTy = varEntry.ty;
    } else if (e.var instanceof Absyn.VarDec) {
        Absyn.VarDec var = (Absyn.VarDec)e.var;
        if (var.typ != null) {
            varTy = transTy(var.typ);
        } else {
            varTy = INT;
        }
        env.venv.put(var.name, new VarEntry(varTy));
    } else {
        error(e.var.pos, "Invalid loop variable declaration");
    }
    ExpTy hi = transExp(e.hi);
    checkInt(hi, e.hi.pos);
    ExpTy body = transExp(e.body);
    env.venv.endScope();
    return new ExpTy(null, body.ty);
  }
 
  private ExpTy transVar(Absyn.Var v){
    if (v instanceof Absyn.SimpleVar)
      return transVar((Absyn.SimpleVar)v);
    else if (v instanceof Absyn.FieldVar)
      return transVar((Absyn.FieldVar)v);
    else if (v instanceof Absyn.SubscriptVar)
      return transVar((Absyn.SubscriptVar)v);
    else throw new Error("Variable of unknown type.");

  }

  ExpTy transVar(Absyn.SimpleVar v) {
    Entry x = (Entry)env.venv.get(v.name);
    if (x == null) {
      error(v.pos, "Variable " + v.name + " is undefined.");
      return new ExpTy(null, VOID);
    } else if (x instanceof VarEntry) {
      VarEntry vent = (VarEntry) x;
      return new ExpTy(null, vent.ty.actual());
    } else {
      error(v.pos, "Expected variable, but found function " + v.name + ".");
      return new ExpTy(null, VOID);
    }
  }

  ExpTy transVar(Absyn.FieldVar v) {
    Types.Type temp = transVar(v.var).ty.actual();
    if (!(temp instanceof Types.RECORD)) {
      error(v.var.pos, "No record");
      return new ExpTy(null, INT);
    }
    for(Types.RECORD i = (Types.RECORD)temp; i != null; i = i.tail) {
      return new ExpTy(null, i.actual());
    }
    return new ExpTy(null, INT);
  }

  ExpTy transVar(Absyn.SubscriptVar v) {
    ExpTy var = transVar(v.var);
    ExpTy index = transExp(v.index);

    if (!(var.ty instanceof Types.ARRAY)) {
      error(v.var.pos, "Variable is not an array");
      return new ExpTy(null, INT);  
    }
    if (!(index.ty instanceof Types.INT)) {
      error(v.index.pos, "Index is not an integer");
      return new ExpTy(null, INT);
    }
    Types.ARRAY at = (Types.ARRAY) var.ty;
    return new ExpTy(null, at.element);
  }

  Exp transDec(Absyn.Dec d) {
    if (d instanceof Absyn.VarDec)
      return transDec((Absyn.VarDec)d);
    if (d instanceof Absyn.TypeDec)
      return transDec((Absyn.TypeDec)d);
    if (d instanceof Absyn.FunctionDec)
      return transDec((Absyn.FunctionDec)d);
    throw new Error("Semant.transDec");
  }

  Exp transDec(Absyn.VarDec d) {
    ExpTy init = transExp(d.init);
    Type type;
    if (d.typ == null) {
      if(init.ty.coerceTo(NIL)){
        error(d.pos, "No record type :(");
      }
      type = init.ty;
    } else {
      type = transTy(d.typ);
      if(type == null) {
        error(d.pos, "Undefined type:(");
      }
      if(!init.ty.coerceTo(type)) {
        error(d.pos, "Incompatable types :(");
      }
    }
    d.entry = new VarEntry(type);
    env.venv.put(d.name, d.entry);
    return null;
  }

  Exp transDec(Absyn.TypeDec d) { 
    for (Absyn.TypeDec type = d; type != null; type = type.next) {
        for (Absyn.TypeDec x = d; x != type; x = x.next) {
            if (x.name == type.name) {
                error(x.pos, "Type '" + x.name + "' already declared!");
            }
        }
        env.tenv.put(type.name, transTy(type.ty));
    }
    return null;
  }

  Exp transDec(Absyn.FunctionDec d) {
    Types.Type result = transTy(d.result);
    Types.RECORD formals = transTypeFields(d.params);
    env.venv.put(d.name, new FunEntry(formals, result));
    env.venv.beginScope();
    for(Absyn.FieldList p = d.params; p!= null; p = p.tail) {
      env.venv.put(p.name, new VarEntry((Types.Type)env.tenv.get(p.typ)));
    }
    transExp(d.body);
    env.venv.endScope();
    return null;
  }

  Type transTy(Absyn.Ty t){ 
      if ((t instanceof Absyn.NameTy)) {
        return transTy((Absyn.NameTy)t);
      }
      if ((t instanceof Absyn.RecordTy)) {
         return transTy((Absyn.RecordTy)t);
      }
      if ((t instanceof Absyn.ArrayTy)) {
         return transTy((Absyn.ArrayTy)t);
       }
      throw new Error("Error translating type :(");
  }

  Type transTy(Absyn.NameTy t) {
    Types.Type type = (Types.Type)env.tenv.get(t.name);
    if (type == null){
      error(t.pos, "The type " + t.name + "is not defined!");
      type = INT;
    }
    return type;
  }

  Type transTy(Absyn.RecordTy t) {
    if (t == null || t.fields == null) {
        error(t.pos, "Record can't be empty");
        return null;
    }
    return transTypeFields(t.fields);
  }

  Type transTy(Absyn.ArrayTy t) {
    Absyn.ArrayTy arrayTy = (Absyn.ArrayTy) t;
    Types.Type elem = (Types.Type)env.tenv.get(arrayTy.typ);
    if (elem == null)
    {
      error(t.pos, "Unknown type");
    }
    return new Types.ARRAY(elem);
  }

  Types.RECORD transTypeFields(Absyn.FieldList f) {
    Types.Type temp = (Types.Type)env.tenv.get(f.typ);
    if (f.tail != null)
      return new Types.RECORD(f.name, temp, transTypeFields(f.tail));
    else  
      return null;
  }

}

