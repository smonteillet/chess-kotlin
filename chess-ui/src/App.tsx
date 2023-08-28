import React from 'react';
import GamePage from './page/GamePage';
import {createBrowserRouter, RouterProvider} from 'react-router-dom';
import CreateGamePage from './page/CreateGamePage';

function App() {

  const router = createBrowserRouter([
    {
      path: "/",
      element: <CreateGamePage />,
    },
    {
      path: ":gameId",
      element: <GamePage />,
    },
  ]);

  return (
      <RouterProvider router={router} />
  );
}

export default App;
