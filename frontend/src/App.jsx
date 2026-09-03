import { BrowserRouter, Routes, Route } from "react-router-dom";

import Home from "./pages/Home/Home";
import Login from "./pages/Login/Login";
import Register from "./pages/Register/Register";

import Dashboard from "./pages/Dashboard/Dashboard";

import Jobs from "./pages/Jobs/Jobs";
import JobDetails from "./pages/JobDetails/JobDetails";

import MyApplications from "./pages/MyApplications/MyApplications";
import Applications from "./pages/Applications/Applications";

import MyProfile from "./pages/MyProfile/MyProfile";
import CareerAnalysis from "./pages/CareerAnalysis/CareerAnalysis";

import Navbar from "./components/Navbar/Navbar";


function App() {

  return (

    <BrowserRouter>

      <Navbar />

      <Routes>

        {/* Home */}
        <Route
          path="/"
          element={<Home />}
        />

        {/* Dashboard */}
        <Route
          path="/dashboard"
          element={<Dashboard />}
        />

        {/* Authentication */}
        <Route
          path="/login"
          element={<Login />}
        />

        <Route
          path="/register"
          element={<Register />}
        />

        {/* Jobs */}
        <Route
          path="/jobs"
          element={<Jobs />}
        />

        <Route
          path="/jobs/:id"
          element={<JobDetails />}
        />

        {/* Applications */}
        <Route
          path="/my-applications"
          element={<MyApplications />}
        />

        <Route
          path="/applications"
          element={<Applications />}
        />

        {/* Profile */}
        <Route
          path="/profile"
          element={<MyProfile />}
        />

        {/* Career Analysis */}
        <Route
          path="/career-analysis"
          element={<CareerAnalysis />}
        />

      </Routes>

    </BrowserRouter>

  );
}

export default App;