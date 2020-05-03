import React from 'react';
import TextField from '@material-ui/core/TextField';
import Send from '@material-ui/icons/Send';
import styles from './styles';
import { withStyles } from '@material-ui/core/styles';
import axios from 'axios'

class ChatTextBoxComponent extends React.Component {

  constructor() {
    super();
    this.state = {
      chatText: ''
    };
  }

  render() {

    const { classes } = this.props;

    return(
      <div className={classes.chatTextBoxContainer}>
        <TextField
          placeholder='Type your message..' 
          onKeyUp={(e) => this.userTyping(e)}
          id='chattextbox' 
          className={classes.chatTextBox}
          onFocus={this.userClickedInput}>
        </TextField>
        <Send onClick={this.submitMessage} className={classes.sendBtn}></Send>
      </div>
    );
  }
  userTyping = (e) => e.keyCode === 13 ? this.submitMessage() : this.setState({ chatText: e.target.value });
  messageValid = (txt) => txt && txt.replace(/\s/g, '').length;
  userClickedInput = () => this.props.userClickedInputFn();
  submitMessage = () => {
    if(this.messageValid(this.state.chatText)) {
      this.props.submitMessageFn(this.state.chatText);
      document.getElementById('chattextbox').value = '';
    }
    
    console.log("batica " + this.props.receiver)
    const url = process.env.NODE_ENV === 'production' ? "rest/chat/messages/user" : "http://localhost:8080/WAR2020/rest/chat/messages/user"
    axios.post(url, {sender: localStorage.getItem('loginInfo'), receiver: this.props.receiver, text: this.state.chatText})
        .then(res => {
        })
        .catch(err => (alert("Error"), console.log(err)));
  }
}

export default withStyles(styles)(ChatTextBoxComponent);