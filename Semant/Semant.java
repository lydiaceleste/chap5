package Semant;
import Translate.Exp;
import Types.Type;


// LEFT TO COMPLETE
//    Exp: Record, Array, Call, For
//    Var: Field, Subscript
//    Dec: TypeDec, FunctionDec
//    Ty: Name, Record, Array

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
      error(pos, "integer required");
    return et.exp;
  }
  //&& (!(type == Types.RECORD)) && (!(type == Types.ARRAY)) for some reason are not working
  private Exp compCheck(ExpTy et, int pos){
    Type type = et.ty.actual();
    if ((!(type == INT)) && (!(type == STRING)) && 
    (!(type == NIL))) {
      error(pos, "Oh no! Types are not comparable :(");
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
    //cant handle empty else
    ExpTy test = transExp(e.test);
    checkInt(test, e.test.pos);
    ExpTy thenclause = transExp(e.thenclause);
    ExpTy elseclause = transExp(e.elseclause);
    if(elseclause != null) {
        if((!thenclause.ty.coerceTo(elseclause.ty)) && !elseclause.ty.coerceTo(thenclause.ty))
        {
           error(e.pos, "Type Mismatch.");
        }
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

  //mayB use depth for boundary check
  ExpTy transExp(Absyn.BreakExp e) {
    return new ExpTy(null, VOID);
  }

  int depth = 0;
  ExpTy transExp(Absyn.WhileExp e) {
      depth++;
      Type type = transExp(e.test).ty;
      if(type != INT) {
        error(e.pos, "Test clause MUST be an int :(");
        type = INT;
      }
      depth--;
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

// ExpTy transExp(Absyn.RecordExp e) {}

// ExpTy transExp(Absyn.ArrayExp e) {}

//ExpTy transExp(Absyn.CallExp e) {}
    //in CallExp making sure that in the function call the 
    //passed params match the type of the declared params 

// ExpTy transExp(Absyn.ForExp e) {}
    //use depth
    //maybe start another scope?


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
    if (x instanceof VarEntry) {
      VarEntry vent = (VarEntry)x;
      return new ExpTy(null, vent.ty.actual());
    }
    error(v.pos, "Variable is undefined.");
    return new ExpTy(null, INT);
  }

//MUST HAVE A RECORD!!
//  ExpTy transVar(Absyn.FieldVar v) {}


  // ExpTy transVar(Absyn.SubscriptVar v) {
  //   ExpTy var = transVar(v.var);
  //   ExpTy index = transExp(v.in);

  // }


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
    // NOTE: THIS IMPLEMENTATION IS INCOMPLETE
    // It is here to show you the general form of the transDec methods
    ExpTy init = transExp(d.init);
    Type type;
    if (d.typ == null) {
      type = init.ty;
    } else {
      type = VOID;
      throw new Error("unimplemented");
    }
    d.entry = new VarEntry(type);
    env.venv.put(d.name, d.entry);
    return null;
  }

//Exp transDec(Absyn.TypeDec d) { }
//Exp transDec(Absyn.FunctionDec d) {




// Type transTy(Ty t){ 
//     if ((t instanceof NameTy)) {
//       return transTy((NameTy)t);
//     }
//     if ((t instanceof RecordTy)) {
//       return transTy((RecordTy)t);
//     }
//     if ((t instanceof ArrayTy)) {
//       return transTy((ArrayTy)t);
//     }
//     throw new Error("Error translating type :(");
// }

//Ty transTy(Absyn.NameTy t) {}
//Ty transTy(Absyn.ArrayTy t) {}
//Ty transTy(Absyn.RecordTy t) {}
}

