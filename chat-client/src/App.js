import React from 'react';
import logo from './logo.svg';
import './App.css';
import Login from './components/login/login';
import Register from './components/register/register';
import {BrowserRouter } from 'react-router-dom'
import { Switch, Route } from 'react-router';
import Dashboard from './components/dashboard/dashboard';

class App extends React.Component {
  /*constructor(props){
    super(props);

    this.state = {
      chatsovi: [
        {
          participants: ["batique", "perfex021"],
          receiverHasRead: false,
          messages: [
            {
              text: "Hey world!",
              sender: "batique",
              date: new Date(2020, 2, 24, 10, 33, 30, 0)
            },
            {
              text: "Hola amigo!",
              sender: "perfex021",
              date: new Date(2020, 2, 24, 10, 34, 30, 0)
            }
          ]
        },
        {
          participants: ["deki", "batique"],
          receiverHasRead: false,
          messages: [
            {
              text: "Cao buraz!",
              sender: "deki",
              date: new Date(2020, 2, 24, 10, 33, 30, 0)
            },
            {
              text: "Eee brate!",
              sender: "batique",
              date: new Date(2020, 2, 24, 10, 34, 30, 0)
            }
          ]
        },
        {
          participants: ["deki", "perfex021"],
          receiverHasRead: false,
          messages: [
            {
              text: "Cao buraz!",
              sender: "deki",
              date: new Date(2020, 2, 24, 10, 33, 30, 0)
            },
            {
              text: "Eee brate!",
              sender: "perfex021",
              date: new Date(2020, 2, 24, 10, 34, 30, 0)
            }
          ]
        }
      ],
      loggedInUser: "batique",
      users: [
        {
          id: 0,
          username: "batique",
          password: "batique"
        },
        {
          id: 1,
          username: "perfex021",
          password: "perfex021"
        },
        {
          id: 2,
          username: "rastko",
          password: "rastko"
        },
        {
          id: 3,
          username: "deki",
          password: "deki"
        }
      ],
      /*groups: [
        {
          groupId: 0,
          participantIds: [0, 1]
        },
        {
          groupId: 1,
          participantIds: [0, 2]
        }
      ],
      messages: [
        {
          messageId: 0,
          senderId: 0,
          groupId: 0,
          text: 'Hello World 1!',
          date: new Date(2020, 2, 24, 10, 33, 30, 0)
        },
        {
          messageId: 1,
          senderId: 3,
          groupId: 0,
          text: 'Hello World 2!',
          date: new Date(2020, 2, 24, 10, 34, 30, 0)
        },
        {
          messageId: 2,
          senderId: 0,
          groupId: 0,
          text: 'Hello World 3!',
          date: new Date(2020, 2, 24, 10, 35, 30, 0)
        },
        {
          messageId: 3,
          senderId: 2,
          groupId: 1,
          text: 'Hello World 4!',
          date: new Date(2020, 2, 24, 10, 36, 30, 0)
  
        },
        {
          messageId: 4,
          senderId: 0,
          groupId: 1,
          text: 'Hello World 5!',
          date: new Date(2020, 2, 24, 10, 37, 30, 0)
        }
      ]
    }
  }*/

  render(){
    return (
      <div id="routing-component">
        <BrowserRouter>
          <Switch>
            <Route exact path="/" component={Login}></Route>
            <Route path="/login" component={Login}></Route>
            <Route path="/register" component={Register}></Route>
            <Route path="/dashboard" component={Dashboard}></Route>
          </Switch>
        </BrowserRouter>
      </div>
    );
  }
}

export default App;
