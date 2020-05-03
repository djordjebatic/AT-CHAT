import { Link } from 'react-router-dom';
import React from 'react';
import styles from './styles';
import FormControl from '@material-ui/core/FormControl';
import InputLabel from '@material-ui/core/InputLabel';
import Input from '@material-ui/core/Input';
import Paper from '@material-ui/core/Paper';
import withStyles from '@material-ui/core/styles/withStyles';
import CssBaseline from '@material-ui/core/CssBaseline';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import axios from 'axios'

class Login extends React.Component {

  constructor() {
    super();
    this.state = {
      email: null,
      password: null,
      serverError: false
    };
  }

  render() {

    const { classes } = this.props;

    return (
      <main className={classes.main}>
        <CssBaseline/>
        <Paper className={classes.paper}>
          <Typography component="h1" variant="h5">
            Welcome to Chat App
          </Typography>
          <form onSubmit={(e) => this.submitLogin(e)} className={classes.form}>
            <FormControl required fullWidth margin='normal'>
              <InputLabel htmlFor='login-email-input'>Username</InputLabel>
              <Input autoComplete='email' autoFocus onChange={(e) => this.userTyping('email', e)} id='login-email-input'></Input>
            </FormControl>
            <FormControl required fullWidth margin='normal'>
              <InputLabel htmlFor='login-password-input'>Password</InputLabel>
              <Input autoComplete="current-password" type="password" onChange={(e) => this.userTyping('password', e)} id='login-password-input'></Input>
            </FormControl>
            <Button type='submit' fullWidth variant='contained' color='primary' className={classes.submit}>Log In</Button>
          </form>
          { this.state.serverError ? 
            <Typography className={classes.errorText} component='h5' variant='h6'>
              Incorrect Login Information
            </Typography> :
            null
          }
          <h5 className={classes.noAccountHeader}>Don't Have An Account? <Link className={classes.signUpLink} to='/register'><b>Register</b></Link></h5>
        </Paper>
      </main>
    );
  }

  userTyping = (whichInput, event) => {
    switch (whichInput) {
      case 'email':
        this.setState({ email: event.target.value });
        break;

      case 'password':
        this.setState({ password: event.target.value });
        break;

      default:
        break;
    }
  }

  submitLogin = (e) => {
    e.preventDefault();

    var user = {username: this.state.email, password: this.state.password}
    console.log(user)
    const url = process.env.NODE_ENV === 'production' ? "rest/chat/users/login" : "http://localhost:8080/WAR2020/rest/chat/users/login"
    axios.post(url, user)
    .then(res => {
      localStorage.setItem('loginInfo', this.state.email)
      this.props.history.push('/dashboard')
    })
    .catch(err => (alert("Wrong username/password"), console.log(err)));
  };

}

export default withStyles(styles)(Login);