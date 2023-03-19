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
      error(pos, "integer required");
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
    default:
      throw new Error("unknown operator");
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

  // ExpTy transExp(Absyn.ArrayExp e) {

  // }

  // ExpTy transExp(Absyn.AssignExp e) {

  // }

  // ExpTy transExp(Absyn.BreakExp e) {

  // }

  // ExpTy transExp(Absyn.CallExp e) {

  // }

  // ExpTy transExp(Absyn.ForExp e) {

  // }

  ExpTy transExp(Absyn.IfExp e) {
    ExpTy test = transExp(e.test);
    checkInt(test, e.test.pos);
    ExpTy thenclause = transExp(e.thenclause);
    ExpTy elseclause = transExp(e.elseclause);

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

  // ExpTy transExp(Absyn.VarExp e) {
  //   return (e.var);
  // }

  // ExpTy transExp(Absyn.RecordExp e) {

  // }

  // ExpTy transExp(Absyn.SeqExp e) {

  // }



  Exp transDec(Absyn.Dec d) {
    if (d instanceof Absyn.VarDec)
      return transDec((Absyn.VarDec)d);
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

}

