import React from 'react';
import { FormControl, InputLabel, Input, Button, Paper, withStyles, CssBaseline, Typography } from '@material-ui/core';
import styles from './styles';
import axios from 'axios'

class NewChatComponent extends React.Component {

  constructor() {
    super();
    this.state = {
      username: null,
      message: null
    };
  }

  render() {

    const { classes } = this.props;

    return(
      <main className={classes.main}>
        <CssBaseline/>
        <Paper className={classes.paper}>
          <Typography component="h1" variant="h5">Send A Message!</Typography>
          <form className={classes.form} onSubmit={(e) => this.submitNewChat(e)}>
            <FormControl fullWidth>
              <InputLabel htmlFor='new-chat-username'>
                  Friend's Username (*if you want to send to all)
              </InputLabel>
              <Input required 
                className={classes.input}
                autoFocus 
                onChange={(e) => this.userTyping('username', e)} 
                id='new-chat-username'>
              </Input>
            </FormControl>
            <FormControl fullWidth>
              <InputLabel htmlFor='new-chat-message'>
                  Message
              </InputLabel>
              <Input required 
                className={classes.input}
                onChange={(e) => this.userTyping('message', e)} 
                id='new-chat-message'>
              </Input>
            </FormControl>
            <Button fullWidth variant='contained' color='primary' className={classes.submit} type='submit'>Send</Button>
          </form>
          {
            this.state.serverError ? 
            <Typography component='h5' variant='h6' className={classes.errorText}>
              Unable to locate the user
            </Typography> :
            null
          }
        </Paper>
      </main>
    );
  }

  componentDidMount() {

  }

  userTyping = (inputType, e) => {
    switch (inputType) {
      case 'username':
        this.setState({ username: e.target.value });
        break;
      
      case 'message':
        this.setState({ message: e.target.value });
        break;

      default:
        break;
    }
  }

  submitNewChat = async (e) => {
    e.preventDefault();
    const userExists = await this.userExists();
    if(userExists) {
      const chatExists = await this.chatExists();
      chatExists ? this.goToChat() : this.createChat();
    }

    var sender = localStorage.getItem('loginInfo');

    if (sender === this.state.username){
      alert("Haha Speaking with Yourself!")
      return
    }

    if (this.state.username === "*"){
      const url = process.env.NODE_ENV === 'production' ? "rest/chat/messages/allUsers" : "http://localhost:8080/WAR2020/rest/chat/messages/allUsers"
      axios.post(url, {sender: sender, text: this.state.message})
    .then(res => {
      alert('Message sent! Please refresh the page (WebSocket errors bugs still not solved)')
    })
    .catch(err => (alert("Error"), console.log(err)));
    }
    else {
    const urlExists = process.env.NODE_ENV === 'production' ? "rest/chat/users/exists/" : "http://localhost:8080/WAR2020/rest/chat/users/exists/"
    axios.get(urlExists + this.state.username)
    .then(response => {
      console.log(response.data);
      if (response.data === "no"){
        alert("User doesn't exist!")
        return
      }
      else {
        const urlUser = process.env.NODE_ENV === 'production' ? "rest/chat/messages/user" : "http://localhost:8080/WAR2020/rest/chat/messages/user"

        axios.post(urlUser, {sender: sender, receiver: this.state.username, text: this.state.message})
        .then(res => {
          alert('Message sent! Please refresh the page (WebSocket errors bugs still not solved)')
        })
        .catch(err => (alert("Error"), console.log(err)));
      }
    });
    }
  }

  buildDocKey = () => [true, this.state.username].sort().join(':');

  createChat = () => {
    this.props.newChatSubmitFn({
      sendTo: this.state.username,
      message: this.state.message
    });
  }

  goToChat = () => this.props.goToChatFn(this.buildDocKey(), this.state.message);

  chatExists = async () => {
    const docKey = this.buildDocKey();
    const chat = await 
    console.log(chat.exists);
    return chat.exists;
  }
  userExists = async () => {
  }
}

export default withStyles(styles)(NewChatComponent);