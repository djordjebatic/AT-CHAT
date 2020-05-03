import React from 'react';
import NewChatComponent from '../newChat/newChat';
import ChatListComponent from '../chatList/chatList';
import ChatViewComponent from '../chatView/chatView';
import ChatTextBoxComponent from '../chatTextBox/chatTextBox';
import styles from './styles';
import { Button, withStyles } from '@material-ui/core';
import axios from 'axios'
import { withRouter } from 'react-router-dom';

class DashboardComponent extends React.Component {

  constructor() {
    super();
    this.state = {
      selectedChat: null,
      newChatFormVisible: false,
      email: null,
      friends: [],
      chats: [],
      user: {}
      };
  }

  websocket;

  render() {
    console.log("User: " + localStorage.getItem('loginInfo'))

    const { classes } = this.props;

    var loggedInUser = localStorage.getItem("loginInfo")
    if(loggedInUser) {
      return(
        <div className='dashboard-container' id='dashboard-container'>
          <ChatListComponent history={this.props.history} 
            userEmail={loggedInUser} 
            selectChatFn={this.selectChat} 
            chats={this.state.chats}
            selectedChatIndex={this.state.selectedChat}
            newChatBtnFn={this.newChatBtnClicked}>
          </ChatListComponent>
          {
            this.state.newChatFormVisible ? null : <ChatViewComponent 
              user={loggedInUser} 
              chat={this.state.chats[this.state.selectedChat]}>
            </ChatViewComponent>
          }
          { 
            this.state.selectedChat !== null && !this.state.newChatFormVisible ? <ChatTextBoxComponent userClickedInputFn={this.messageRead} submitMessageFn={this.submitMessage} receiver={this.state.chats[this.state.selectedChat].participants.filter(_usr => _usr !== localStorage.getItem('loginInfo'))[0]}></ChatTextBoxComponent> : null 
          }
          {
            this.state.newChatFormVisible ? <NewChatComponent goToChatFn={this.goToChat} newChatSubmitFn={this.newChatSubmit}></NewChatComponent> : null
          }
          <Button onClick={this.loggedIn} className={classes.loggedInBtn}>Logged In Users</Button>
          <Button onClick={this.registered} className={classes.registeredBtn}>Registered Users</Button>
          <Button onClick={this.signOut} className={classes.signOutBtn}>Sign Out</Button>
        </div>
      );
    } else {
      return(<div>You can't access this page without logging in!</div>);
    }
  }

  signOut = () => {
    const url = process.env.NODE_ENV === 'production' ? "rest/chat/users/loggedIn/" : "http://localhost:8080/WAR2020/rest/chat/users/loggedIn/"
    axios.delete(url + localStorage.getItem('loginInfo'))
    .then(response => {
      alert('Log out successful');
    });
    localStorage.removeItem('loginInfo');
    this.props.history.push('/');
  };

  loggedIn = () => {
    const url = process.env.NODE_ENV === 'production' ? "rest/chat/users/loggedIn" : "http://localhost:8080/WAR2020/rest/chat/users/loggedIn"
    axios.get(url)
    .then(response => {
      alert("Check WildFly to see logged in users.")
    });
  };

  registered = () => {
    const url = process.env.NODE_ENV === 'production' ? "rest/chat/users/registered" : "http://localhost:8080/WAR2020/rest/chat/users/registered"
    axios.get(url)
    .then(response => {
      alert("Check WildFly to see registered users.")
    });
  };

  submitMessage = (msg) => {
  }

  // Always in alphabetical order:
  // 'user1:user2'
  buildDocKey = (friend) => [this.state.email, friend].sort().join(':');

  newChatBtnClicked = () => this.setState({ newChatFormVisible: true, selectedChat: null });

  newChatSubmit = async (chatObj) => {
  }

  selectChat = async (chatIndex) => {
    await this.setState({ selectedChat: chatIndex, newChatFormVisible: false });
    this.messageRead();
  }

  goToChat = async (docKey, msg) => {
    const usersInChat = docKey.split(':');
    const chat = this.state.chats.find(_chat => usersInChat.every(_user => _chat.users.includes(_user)));
    this.setState({ newChatFormVisible: false });
    await this.selectChat(this.state.chats.indexOf(chat));
    this.submitMessage(msg);
  }

  // Chat index could be different than the one we are currently on in the case
  // that we are calling this function from within a loop such as the chatList.
  // So we will set a default value and can overwrite it when necessary.
  messageRead = () => {
    const chatIndex = this.state.selectedChat;
    const docKey = this.buildDocKey(this.state.chats[chatIndex].participants.filter(_usr => _usr !== localStorage.getItem("loginInfo"))[0]);
    if(this.clickedMessageWhereNotSender(chatIndex)) {
    } else {
      console.log('Clicked message where the user was the sender');
    }
  }

  clickedMessageWhereNotSender = (chatIndex) => this.state.chats[chatIndex].messages[this.state.chats[chatIndex].messages.length - 1].sender !== localStorage.getItem("loginInfo");

  componentWillMount() {

    var loc = window.location, uri;
    uri = "ws:";
    uri += "//" + loc.host;
    uri += loc.pathname + "ws/";
    console.log(uri);
    const client = process.env.NODE_ENV === 'production' ? uri : "ws://localhost:8080/WAR2020/ws/";
        
    this.websocket = new WebSocket(client + localStorage.getItem('loginInfo'));

    this.websocket.onopen = () => {
      console.log('connected socket for user ' + localStorage.getItem('loginInfo'))
      const url = process.env.NODE_ENV === 'production' ? "rest/chat/chats/" : "http://localhost:8080/ChatWar/rest/chat/chats/";
      axios.get(url + localStorage.getItem('loginInfo'))
      .then(response => {
        console.log(response.data);
        this.setState({chats: response.data})
      });
    }

    this.websocket.onmessage = evt => {
        console.log('socket onmessage method called')
        const url = process.env.NODE_ENV === 'production' ? "rest/chat/chats/" : "http://localhost:8080/ChatWar/rest/chat/chats/";
        axios.get(url + localStorage.getItem('loginInfo'))
        .then(response => {
          console.log(response.data);
          this.setState({chats: response.data})
        });
    }

    this.websocket.onclose = () => {
        console.log('disconnected socket')
        this.setState({
            websocket: new WebSocket(client + localStorage.getItem('loginInfo')),
        })
    }
  }
}

export default withRouter (withStyles(styles)(DashboardComponent));