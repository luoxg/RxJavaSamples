package pers.example.xiayong.rxjavasamples.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.jakewharton.rxbinding.widget.RxTextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import pers.example.xiayong.rxjavasamples.R;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.functions.Func3;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;
import static android.util.Patterns.EMAIL_ADDRESS;

public class FormValidationCombineLatestFragment
      extends BaseFragment {

    @Bind(R.id.btn_demo_form_valid) TextView _btnValidIndicator;
    @Bind(R.id.demo_combl_email) EditText _email;
    @Bind(R.id.demo_combl_password) EditText _password;
    @Bind(R.id.demo_combl_num) EditText _number;

    private Observable<CharSequence> _emailChangeObservable;
    private Observable<CharSequence> _passwordChangeObservable;
    private Observable<CharSequence> _numberChangeObservable;

    private Subscription _subscription = null;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_form_validation_comb_latest,
              container,
              false);
        ButterKnife.bind(this, layout);
        /*_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d("FormValidation","beforeTextChanged");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("FormValidation","onTextChanged");
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d("FormValidation","afterTextChanged");
            }
        });*/
        _emailChangeObservable = RxTextView.textChanges(_email).skip(1);
        _passwordChangeObservable = RxTextView.textChanges(_password).skip(1);
        _numberChangeObservable = RxTextView.textChanges(_number).skip(1);

        _combineLatestEvents();

        return layout;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (_subscription != null) {
            _subscription.unsubscribe();
        }
    }

    private void _combineLatestEvents() {
        _subscription = Observable.combineLatest(_emailChangeObservable,
              _passwordChangeObservable,
              _numberChangeObservable,
              new Func3<CharSequence, CharSequence, CharSequence, Boolean>() {
                  @Override
                  public Boolean call(CharSequence newEmail,
                                      CharSequence newPassword,
                                      CharSequence newNumber) {

                      Log.d("xiayong",newEmail+" "+newPassword+" "+newNumber);
                      boolean emailValid = !isEmpty(newEmail) &&
                                           EMAIL_ADDRESS.matcher(newEmail).matches();
                      if (!emailValid) {
                          _email.setError("Invalid Email!");
                      }

                      boolean passValid = !isEmpty(newPassword) && newPassword.length() > 8;
                      if (!passValid) {
                          _password.setError("Invalid Password!");
                      }

                      boolean numValid = !isEmpty(newNumber);
                      if (numValid) {
                          int num = Integer.parseInt(newNumber.toString());
                          numValid = num > 0 && num <= 100;
                      }
                      if (!numValid) {
                          _number.setError("Invalid Number!");
                      }

                      return emailValid && passValid && numValid;

                  }
              })//
              .subscribe(new Observer<Boolean>() {
                  @Override
                  public void onCompleted() {
                      Timber.d("completed");
                  }

                  @Override
                  public void onError(Throwable e) {
                      Timber.e(e, "there was an error");
                  }

                  @Override
                  public void onNext(Boolean formValid) {
                      Log.d("xiayong","formValid"+formValid);
                      if (formValid) {
                          _btnValidIndicator.setBackgroundColor(getResources().getColor(R.color.blue));
                      } else {
                          _btnValidIndicator.setBackgroundColor(getResources().getColor(R.color.gray));
                      }
                  }
              });
    }
}
